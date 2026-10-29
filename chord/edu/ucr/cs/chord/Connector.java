package edu.ucr.cs.chord;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

/**
   Provides a facility for sending messages to other nodes.

   There is support for both synchronous and asynchronous transport. 

   TODO: Have a separate thread clean up failed connections now and then.
         should probably shut down inactive connections too.

   This class could use some more work. There are a lot of TODO items
   down there, and since it's a rather complex piece of code it has to
   be even better documented and tested.
*/

class Connector
{
      public static final int MESSAGE_TIMEOUT=10000;
      public static final int INITIAL_BUFFER_SIZE=1024; 

      Selector channelSelector;
      Selector handshakeSelector;

      EndPoint localHost;

      LinkedList outputQueue=new LinkedList();
      LinkedList inputQueue=new LinkedList();

      /* 
	 The message listener gets any message that didn't have an
	 action in the actiontable.
      */
      MessageListener messageListener;

      /** 
	  A map of EndPoints to Channels. Upon sending a message, 
	  the Connector looks at the nextDestination EndPoint and
	  tries to find the best way there.

	  TODO: Still no garbage collection of connections!
      */
      HashMap socketChannels=new HashMap();

      /**
	 Keeps track of failed nodes. 
	 TODO: Should really support retries, and stop blocking after a little while.
      */
      HashSet failedNodes=new HashSet();

      /**
	 General Datagram channel for sending/receiving packets from
	 non-connected peers.
      */
      DatagramChannel commonDatagramChannel;

      /**
	 Listens to the given port on TCP accepting new connections.
      */
      ServerSocketChannel serverChannel;

      /** 
	  Maps sequenceNumbers to Actions, 
	  usually a Notify lock object.
	 
	  TODO: Need a timeout/garbage collect on this. 
	  Messages that don't get replies will leave 
	  Actions in the table.
      */
      HashMap actionTable=new HashMap();

      Receiver receiver;

      Charset charset = Charset.forName("ISO-8859-1");
      CharsetEncoder encoder = charset.newEncoder();     
      CharsetDecoder decoder = charset.newDecoder();

      public Connector(EndPoint localHost) throws IOException
      {
	 this.localHost=localHost;

	 channelSelector=Selector.open();
	 handshakeSelector=Selector.open();

	 commonDatagramChannel=DatagramChannel.open();

	 // listen to the indicated port;
	 commonDatagramChannel.socket().bind(localHost.getSocketAddress());
	 commonDatagramChannel.configureBlocking(false);
	 commonDatagramChannel.register(channelSelector, SelectionKey.OP_READ|SelectionKey.OP_WRITE, new ChannelState(null));	 

	 serverChannel=ServerSocketChannel.open();
	 serverChannel.socket().bind(localHost.getSocketAddress());	    
	 serverChannel.configureBlocking(false);
	 serverChannel.register(handshakeSelector, SelectionKey.OP_ACCEPT);	 

	 receiver=new Receiver();
	 receiver.start();
      }

      /**
	 Opens a TCP connection to the given EndPoint. Once the TCP connection
	 is open, all messages to that EndPoint will travel through the TCP connection.

	 During connection setup, however, packets will keep traveling the UDP way.
      */
      
      public void prepareSocket(EndPoint remote) throws IOException
      {
	 System.err.println("Preparing socket to: "+remote);
	 assert !socketPrepared(remote) : "Can't prepare socket twice!";

	 SocketChannel channel=SocketChannel.open();
	 channel.configureBlocking(false);
	 channel.connect(new InetSocketAddress(remote.getHostAddress(),remote.getPort()));
	 channel.register(handshakeSelector, SelectionKey.OP_CONNECT, new ChannelState(remote));	 	    	    	 	 
	 
  	 socketChannels.put(remote,channel);
      }

      public boolean socketPrepared(EndPoint remote)
      {
	 return socketChannels.containsKey(remote);
      }

      public void reportFailure(EndPoint node)
      {
	 System.err.println("Received failure report for: "+node);

	 SocketChannel sc=(SocketChannel)socketChannels.remove(node);
	 if(sc!=null) try {
	    SelectionKey key=sc.keyFor(channelSelector);
	    if(key!=null) key.cancel();

	    key=sc.keyFor(handshakeSelector);
	    if(key!=null) key.cancel();

	    sc.socket().shutdownInput();	    
	    sc.socket().shutdownOutput();
	    sc.socket().close();
	 } catch(IOException e){}
	 
	 failedNodes.add(node);
      }

      public boolean hasFailed(EndPoint node)
      {
	 return failedNodes.contains(node);
      }

      public void setMessageListener(MessageListener l) {
	 this.messageListener=l;
      }

      public String toString() {
	 return "Connector\n--------- \nsocketChannels: "+socketChannels.keySet()+" \nfailed: "+failedNodes;
      }
      
      /**
	 Sends a message and returns the reply when it arrives. This
	 method returns only after receiving the reply.
      */
      public Reply transcieve(Message m) throws TimeoutException
      {
	 /*
	   1. Send the message.
	 */

	 ReplyNotification n=new ReplyNotification();
	 transmit(m,n);

	 /*
	   2. Wait for the reply.
	 */

	 try { 
	    synchronized(n) {
	       n.wait(MESSAGE_TIMEOUT);
	    }
	 }catch(InterruptedException e) {
	    e.printStackTrace(); 
	 }

	 /* clean up the action table. */
	 actionTable.remove(new Short(m.getSequenceNumber())); 
	 
	 /*
	   3. Check if there's a reply for us, otherwise 
	   throw TimeoutException.
	 */
	 
	 Reply r=n.getReply();
	 if(r==null) {

	    System.out.println("Transaction timed out once: "+m);
	    // Fail early. If we don't get our expected reply in time, something is bad with the connection.
	    // shutdown("Message timed out.");
	    throw new TimeoutException(m, "Message reply timed out.");
	 }
	 else 
	    return r;
      } 
      
      /**
	 Convenience method for replies.
      */
      public final void transmitReply(Reply r) throws IOException
      {
	 transmit(r,null);
      }

      /**
	 Transmits message m and performs action a when a reply
	 arrives. This method returns immediately after buffering
	 the message.
      */
      public void transmit(Message m, Action a) 
      {
	 m.setSource(localHost);

	 if(a!=null) 
	    actionTable.put(new Short(m.getSequenceNumber()),a);

	 dispatch(m);
      }
      
      private void dispatch(Message m)
      {
	 // TODO: Optimize away these bytebuffer allocations. 
	 //       Probably keep a synchronized linked list of unused buffers.
	 ByteBuffer buf=ByteBuffer.allocate(m.estimateBufferSize());
	 m.writeMessage(buf);
	 buf.flip();
	 	
	 EndPoint destination=m.getNextDestination();
	 assert destination!=null : "Messages must have a set destination before dispatch.";

	 SelectableChannel channel=(SelectableChannel)socketChannels.get(destination);
	 
	 if(channel==null) channel=commonDatagramChannel;

	 SelectionKey key=channel.keyFor(channelSelector);	 

	 if(key==null) { 
	    channel=commonDatagramChannel;
	    key=channel.keyFor(channelSelector);
	 }

	 ChannelState state=(ChannelState)key.attachment();
	 
	 if(state.getOutputBuffer() == null) 
	 {	 
	    try{
	       if(channel==commonDatagramChannel) {
		  InetSocketAddress address=new InetSocketAddress(destination.getHostAddress(),destination.getPort());
		  commonDatagramChannel.send(buf,address);
	     
		  if(!buf.hasRemaining()) return;
		  else if(buf.position()>0)
		     assert false : "Couldn't send complete UDP datagram";
	       }
	       else
	       {
		  SocketChannel sc=(SocketChannel)channel;
		  boolean result;	       
		  
		  if(sc.isConnectionPending() && !sc.finishConnect()) 
		     System.err.println("Connection was not finished...");		 
		  else
		  { 
		     sc.write(buf);
		     
		     // only save the buffer if we started writing it and there something left in it.
		     if(!buf.hasRemaining()) 
			return;
		     else if(buf.position()>0) 
		     {		    
			state.setOutputBuffer(buf);
			System.out.println("Wrote partial message "+m+" to buffer! Saving state for later...");
			
			return;
		     }
		     else 
			System.err.println("Connection was not finished...");
		  }
	       }

	       // if we couldn't send it immediately, save it for later
	       if(key!=null) {
		  // since we didn't manage to send the message, put it in the channel queue
		  synchronized(state) {				 
		     state.addToQueue(m);
		     //  and start waiting for chance to send.
		     key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		  }
	       }
	       // if we don't have a key for this channel yet, register it in the common channel queue.
	       else {
		  //TODO: this is kind of an ugly trick. For channels that aren't yet registered with the channelSelector 
		  //      (those that haven't completed handshaking) we put the message in the common outQueue. This means it may
		  //      get tried a bunch of times before the channel becomes available.		  		  
		  key=commonDatagramChannel.keyFor(channelSelector);	       
		  
		  synchronized(state) {				 
		     state.addToQueue(m);
		     //  and start waiting for chance to send.
		     key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		  }
	       }		  	       
	    }catch(IOException e) {
	       e.printStackTrace(); //TODO: get rid of this line
	       messageListener.messageDeliveryFailed(m);
	    }
	 }	
 
	 // since we didn't manage to send the message, put it in the channel queue
	 synchronized(state) {			 
	    state.addToQueue(m);
	    
	    //  and start waiting for chance to send.
	    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	 }	 
      }

      public static void main(String argv[]) throws Exception
      {
	 if(argv.length==0) 
	 {
	    Connector c=new Connector(new EndPoint(8888, InetAddress.getLocalHost().getAddress()));
	    
	    PingMessage m=new PingMessage();
	    EndPoint destination=new EndPoint(7890, InetAddress.getLocalHost().getAddress());
	    m.setNextDestination(destination);
	    
	    PingReply r=(PingReply)c.transcieve(m);
	    	    
	    System.err.println("Got response: "+r);
	    
	    c.prepareSocket(destination);

	    System.err.println("Waiting for sockets to finish connecting...");
	    Thread.sleep(1000);

	    m=new PingMessage();
	    m.setNextDestination(destination);

	    r=(PingReply)c.transcieve(m);
	    
	    // This response arrived via Sockets (TCP)

	    System.err.println("Got response: "+r);
	 }
	 else if(argv.length==1)
	 {
	    final Connector c=new Connector(new EndPoint(7890, InetAddress.getLocalHost().getAddress()));
	    
	    c.setMessageListener(new MessageListener() {
		     public void messageReceived(Message m) {
			try{
			   System.err.println("Got message: "+m);
			   if(m instanceof PingMessage) {
			      PingReply r=new PingReply(m);
			      c.transmit(r,null);
			   }
			}catch(Exception e) {e.printStackTrace();}		      
		     }
		     public void messageDeliveryFailed(Message m) {}
	       });	    	    
	 }
      }


      class Action {}

      class ReplyNotification extends Action {
	    Reply reply;
	    public void setReply(Reply reply) {
	       this.reply=reply;
	    }
	    public Reply getReply() {
	       return reply;
	    }
      }      
      
      class Receiver extends Thread {

	    boolean keepRunning=true;
	    public void shutdown() {
	       keepRunning=false;
	       this.interrupt();
	    }

	    public void run() 
	    {
	       while(keepRunning) {
		  
		  // loop with timeout to allow registration of new channels.
		  // selection and registration need the same lock.

		  try{
		     
		     // After almost 15 years of programming, here's my first do-while loop that makes sense! Enjoy!
		     do{
			if(handshakeSelector.selectNow()>0)
			   handleHandshakes();
		     }while(channelSelector.select(100)==0);

		  }catch(Exception e) {e.printStackTrace();}
		  
		  Set readyKeys = channelSelector.selectedKeys();
		  Iterator readyItor = readyKeys.iterator();
		  
		  while(readyItor.hasNext()) {
		     try{
			SelectionKey key = (SelectionKey)readyItor.next();
			readyItor.remove();
		     
			Channel keyChannel=key.channel();
		     
			if(key.isReadable()) {			
			   ByteBuffer buf=((ChannelState)key.attachment()).getBuffer();

			   if(keyChannel instanceof SocketChannel) 
			   {
//			      System.err.println("Reading:"+((ChannelState)key.attachment()).getEndPoint());

			      SocketChannel channel=(SocketChannel)keyChannel;
				    
			      try{
				 if(channel.read(buf)<0) {
				    reportFailure(((ChannelState)key.attachment()).getEndPoint());
				 }
				 else {				    
				    // if we have the length field.
				    if(buf.position()>=5) {
				       int length=buf.getInt(1);
//				       System.err.println("Expecting message of type: "+buf.get(0)+" length: "+length+" on channel to:"+((ChannelState)key.attachment()).getEndPoint());

				       // if we have the type, length fields + payload, then go ahead!
				       if(buf.position()>=length+5) {
					  buf.flip();
					  
					  // constrain the message reader to the length of this message.
					  int savedLimit=buf.limit();
					  buf.limit(length+5);
					  
					  Message msg=Message.readMessage(buf);
					  
					  // now restore the buffer limit.
					  buf.limit(savedLimit);
					  
					  // just in case readMessage is incorrect, skip ahead to the end of the message.
					  if(buf.position()!=(length+5))
					  {
					     System.err.println("Warning: Extra bytes in message. Expected payload length: "+length+" actual read bytes: "+(buf.position()-5)+" in message:"+msg);
					     buf.position(length+5);
					  }
					  
					  buf.compact();
					  handleReceivedMessage(msg);
				       }
				    }
				 }				    
			      }catch(IOException e) {
				 // if we have an error while reading, report the node failure immediately.
				 reportFailure(((ChannelState)channel.keyFor(channelSelector).attachment()).getEndPoint());
			      }				 
			   }			      
			   else {
			      DatagramChannel channel=(DatagramChannel)keyChannel;			  
			      channel.receive(buf);			  
				 
			      // In UDP, there are only whole messages in datagrams so we're guaranteed to have
			      // type, length and payload if we have a datagram.
				 
			      // TODO2: May want to verify this.
				 
			      buf.flip();
				 
			      Message msg=Message.readMessage(buf);
			      buf.clear();
			      handleReceivedMessage(msg);
			   }
			}
						   
			/*else*/ // we want to catch both writables and readables at the same time.
			
			// we need to check if the key is still valid, cause it might be canceled
			// in the isReadable() block.
			if(key.isValid() && key.isWritable()) 
			{
			   ChannelState state=(ChannelState)key.attachment();
			   ByteBuffer leftOvers=state.getOutputBuffer();

			   // if the last dispatch left some bytes to be written, write those now.
			   if(keyChannel instanceof SocketChannel && leftOvers!=null)
			   {
			      SocketChannel channel=(SocketChannel)keyChannel;
			      channel.write(leftOvers);
			      if(!leftOvers.hasRemaining()) {
				 System.out.println("finished writing leftovers");
				 state.setOutputBuffer(null);
			      }
			   }
			   else {
			      Message next;
			      synchronized(state) {				 
				 next=state.getNextFromQueue();
				 
				 if(next==null) {
				    // if the queue is empty, we're not that interested in more notification for a while.
				    key.interestOps(SelectionKey.OP_READ);
				 }
			      }
			      
			      if(next!=null) {
//				 System.err.println("Sending message "+next+" from queue.");			      
				 
				 // TODO3: Ok, so this isn't really optimal. We know the channel and therefore don't
				 // need to do some stuff done in dispatch(). On the other hand, it looks nice, doesn't it?
				 
				 dispatch(next);
			      }
			   }
			}
		     }catch(IOException e) {
			System.err.println("WARNING: This exception must be handled properly before release:");
			e.printStackTrace();
		     }
			
		  }		  
	       }
	    }

	    public void handleReceivedMessage(Message m) {
//	       System.err.println("Received: "+m);
//	       System.out.print(".");

	       if(m instanceof Reply) {
		  
		  Action a=(Action)actionTable.get(new Short(((Reply)m).getInResponseTo()));
		  if(a instanceof ReplyNotification) {
		     ((ReplyNotification)a).setReply((Reply)m);
		     synchronized(a) {
			a.notify();
		     }
		  }
		  else
		     messageListener.messageReceived(m);
	       }
	       else {
		  messageListener.messageReceived(m);			   
	       }
	    }

	    void handleHandshakes() throws IOException
	    {
	       Set readyKeys = handshakeSelector.selectedKeys();
	       Iterator readyItor = readyKeys.iterator();
	       
	       while(readyItor.hasNext()) {
		  SelectionKey key = (SelectionKey)readyItor.next();
		  readyItor.remove();

		  System.out.println("Readable: "+key.isReadable()+" Acceptable: "+key.isAcceptable()+" Connectable: "+key.isConnectable());

		  try{
		     if(key.isAcceptable())
		     {
			ServerSocketChannel ss=(ServerSocketChannel)key.channel();
			
			SocketChannel s=ss.accept();
			s.configureBlocking(false);
			
			System.err.println("ACCEPTING CHANNEL");
			
			handshakeSelector.wakeup();
			s.register(handshakeSelector, SelectionKey.OP_READ, new ChannelState(null));
			//ss.register(handshakeSelector, SelectionKey.OP_ACCEPT);
			// TODO: figure out what happens to these channels when the remote endpoint shuts down the socket
		     }
		     
		     else if(key.isConnectable())
		     {
			System.err.println("CONNECTING");
			SocketChannel sc=(SocketChannel)key.channel();
			
			try{
			   while(!sc.finishConnect()) {
				Thread.sleep(100);
			   }
			   
			   System.err.println("Sending handshake");
			    
			// TODO: maybe we can't write here?  
			    
			    int wrote=sc.write(encoder.encode(CharBuffer.wrap("Hello Chord 0.1\nRemoteAddress: "+localHost.getHostAddress()+"\nRemotePort: "+localHost.getPort()+"\n\n")));
			    Thread.sleep(1000);
			    ChannelState attach=(ChannelState)key.attachment();

//			    key.cancel();
//			    sc.register(channelSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, attach);
			    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			}catch(Exception e){}
		     }		    

		     else if((key.interestOps() & SelectionKey.OP_READ)>0)
		     {
			SocketChannel socketChannel=(SocketChannel)key.channel();

			System.out.println("Reading");

			/* if we're registered for connect as well, this is a locally initiated socket */
			if((key.interestOps() & SelectionKey.OP_WRITE)>0)
			{
			   System.out.println("client");
			   ByteBuffer buf=((ChannelState)key.attachment()).getBuffer();
			   socketChannel.read(buf);
			   buf.flip();
			   
			   // TODO: optimize away these charbuffer allocations
			   CharBuffer cbuf=CharBuffer.allocate(19); // WARNING: 19 is the length of "welcome chord 0.1\n\n", UGLY HACK!
			   decoder.decode(buf,cbuf,false);
			   cbuf.flip();
			   String input=cbuf.toString();
			   
			   if(input.endsWith("\n\n")) {
			      buf.clear();
			      socketChannel.register(channelSelector,SelectionKey.OP_READ | SelectionKey.OP_WRITE,key.attachment());
			      key.cancel();
			      
			      InetSocketAddress address=(InetSocketAddress)socketChannel.socket().getRemoteSocketAddress();
			      socketChannels.put(new EndPoint(address.getPort(),address.getAddress().getAddress()), socketChannel);	
			      failedNodes.remove(new EndPoint(address.getPort(),address.getAddress().getAddress()));
			      
			      // TODO: verify the output here, don't just ignore it.
			      // TODO: what if there's a packet right after the \n\n, better be more careful!
			   }
			   else {

			      System.out.println("Waiting for \\n\\n at end of handshake. Now at buf pos: "+buf.position());
			   //   buf.rewind();
			   }

			}
			/* this is for accepted remote connections */
			else 
			{
			   System.out.println("server");
			   ByteBuffer buf=((ChannelState)key.attachment()).getBuffer();
			   
			   socketChannel.read(buf);
			   buf.flip();
			   
			   CharBuffer cbuf=CharBuffer.allocate(1024);
			   decoder.decode(buf,cbuf,false);
			   cbuf.flip();
			   String input=cbuf.toString();
			   System.err.println("GOT:"+input);
			   
			   if(input.endsWith("\n\n")) {
			      
			      StringTokenizer handshakeLines=new StringTokenizer(input,"\n");
			      // leave any remaining bytes for the packet parser
			      buf.clear();
			      
			      String address="";
			      int port=0;
			      
			      while(handshakeLines.hasMoreTokens()) {
				 String line=handshakeLines.nextToken();
				 
				 if(line.startsWith("RemoteAddress: "))
				    address=line.substring("RemoteAddress: ".length());
				 else if(line.startsWith("RemotePort: "))
				    port=Integer.parseInt(line.substring("RemotePort: ".length()));
			      }
			      
			      // TODO: Verify this information 
			      
			      System.err.println("Got address: "+address+" port "+port+" in handshake");
			      // now send response and switch to "receive" mode.
			      
			      socketChannel.write(encoder.encode(CharBuffer.wrap("Welcome Chord 0.1\n\n")));

			      ChannelState attach=(ChannelState)key.attachment();
			      EndPoint remote=new EndPoint(port, address);
			      attach.setEndPoint(remote);

			      // TODO: may want to have a brief pause here to let remote end finish up handshake.

			      key.cancel();
			      socketChannel.register(channelSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, attach);
			      socketChannels.put(remote,socketChannel);			      
			      failedNodes.remove(remote);
			   } else {
			      buf.rewind();
			   }
			}
		     }
		    
		  }catch(Exception e){e.printStackTrace();}
	       }
	       
	    }	       		    
      }
}

class TimeoutException extends IOException
{      
      Message m;
      public TimeoutException(Message m, String msg) {
	 super(msg+": "+m+" en route to "+m.getNextDestination());	 
	 this.m=m;
      }
      public Message getChordMessage() {
	 return m;
      }
}

class MaxRetriesExceededException extends IOException
{
      EndPoint remote;
      public MaxRetriesExceededException(EndPoint remote)
      {
	 super("Connection to "+remote+" has exceeded it's maximum number of retries.");
	 this.remote=remote;
      }  
}

class ChannelState
{
      ByteBuffer buffer;
      ByteBuffer outputBuffer;
      LinkedList outputQueue;
      EndPoint endpoint;
      
      public ChannelState(EndPoint endpoint)
      {
	 buffer=ByteBuffer.allocate(Connector.INITIAL_BUFFER_SIZE);
	 outputQueue=new LinkedList();
	 
	 this.endpoint=endpoint;
      }
      
      public ByteBuffer getBuffer() {
	 synchronized(buffer) {
	    
	    // in case the buffer is full, double its capacity 
	    if(buffer.position()>buffer.limit()/2)
	    {
	       System.out.println("Expanded read buffer capacity.");
	       ByteBuffer oldBuffer=buffer;
	       oldBuffer.flip();
	       buffer=ByteBuffer.allocate(buffer.capacity()*2);
	       buffer.put(oldBuffer);
	    }

	    return buffer;
	 }
      }

      public ByteBuffer getOutputBuffer() {
	 return outputBuffer;
      }

      public void setOutputBuffer(ByteBuffer buf) {
	 this.outputBuffer=buf;
      }

      public EndPoint getEndPoint() {
	 return endpoint;
      }

      /**
	 For updating the endpoint if we didn't know 
	 in advance: this is the case for accepted connections.
      */
      public void setEndPoint(EndPoint endpoint) {
	 this.endpoint=endpoint;
      }
      
      /** 
	  This method is not thread safe. If this, or addToQueue is
	  called at the same time, there could be trouble.
      */
      public Message getNextFromQueue() {
	 if(outputQueue.size()>0)
	    return (Message)outputQueue.removeFirst();	    
	 else 
	    return null;
      }

      /** 
	  This method is not thread safe. If this, or getNextFromQueue is
	  called at the same time, there could be trouble.
      */
      public void addToQueue(Message m) {
	 outputQueue.addLast(m);
      }
}
