package edu.ucr.cs.chord;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.*;

/**
   The superclass of all messages passed in the Chord. Each new message is assigned a unique sequence number, used for Message->Reply mapping. <p>

   Before handing a message to the Connector, the "nextDestination" field must be set. <p>

   |TYPE|PLD LENGTH|SEQ NO|SRC PORT|SRC IP| <br>
   | 1  |     4    |  2   |   2    |  4   |
*/

abstract class Message
{
      public static final byte F_PING=(byte)0x00;
      public static final byte F_PING_REPLY=(byte)0x08;

      public static final byte F_GET_PREDECESSOR=(byte)0x01;
      public static final byte F_GET_PREDECESSOR_REPLY=(byte)0x09;

      public static final byte F_GET_SUCCESSORLIST=(byte)0x02;
      public static final byte F_GET_SUCCESSORLIST_REPLY=(byte)0x0A;

      public static final byte F_FIND_SUCCESSOR=(byte)0x03;
      public static final byte F_FIND_SUCCESSOR_REPLY=(byte)0x0B;

      public static final byte F_SET_PREDECESSOR=(byte)0x04;
      public static final byte F_SET_PREDECESSOR_REPLY=(byte)0x0C;

      public static final byte F_ROUTED_APPLICATION=(byte)0x06;
      public static final byte F_ROUTED_APPLICATION_REPLY=(byte)0x0E;

      public static final byte F_APPLICATION=(byte)0x07;
      public static final byte F_APPLICATION_REPLY=(byte)0x0F;


      public static final int HEADER_LENGTH=1+4; // all we need to read first.

      // IP address of the originator. May or may not be set, depending on transport options.
      private EndPoint source;

      private transient EndPoint nextDestination;

      transient int failureCount;
      public transient long creationTime;

      private short sequenceNumber;
      
      /* anyone accessing nextSequenceNo needs to synchronize over sequenceLock */
      private static short nextSequenceNo=0;
      private static Object sequenceLock=new Object();      

      public Message()
      {
	 creationTime=System.currentTimeMillis();
	 // make sure no two outgoing messages get the same sequence number.
	 synchronized(sequenceLock) {
	    this.sequenceNumber=nextSequenceNo++;
	 }
      }

      public Message(ByteBuffer payload)
      {
	 creationTime=System.currentTimeMillis();
	 sequenceNumber=payload.getShort();
	 source=new EndPoint(payload);	 	 
      }
      
      public short getSequenceNumber() 
      {
	 return sequenceNumber;
      }

      public EndPoint getSource() {
	 return source;
      }
      
      public int estimateBufferSize() {
	 return 1024;
      }

      /**
	 Sets the EndPoint of the originator node, the node that
	 gets any response in the end. This can only be set once.
      */
      public void setSource(EndPoint source) {
	 if(this.source==null) {
//	    System.err.println("Setting source to: "+source);

	    this.source=source;
	 }
      }
      
      public EndPoint getNextDestination() {
	 return nextDestination;
      }

      public void setNextDestination(EndPoint destination) {
//	 System.err.println("Setting next destination to: "+destination);
	 this.nextDestination=destination;
      }
	 
      public static Message readMessage(ByteBuffer buf) throws IOException
      {
	 /*
	   1. Parse the header.
	 */
	 byte func=buf.get();
	 int length=buf.getInt(); // can't handle lengths>2^31-1.

	 ByteBuffer payload=buf;
	 
	 /*
	   2. Parse the payload.
	 */

	 Message m;

	 switch(func) {
	    case F_PING: 
	       m=new PingMessage(payload);
	       break;
	    case F_PING_REPLY:
	       m=new PingReply(payload);
	       break;

	    case F_GET_PREDECESSOR:
	       m=new GetPredecessorMessage(payload);
	       break;
	    case F_SET_PREDECESSOR_REPLY:
	       m=new SetPredecessorReply(payload);
	       break;

	    case F_SET_PREDECESSOR:
	       m=new SetPredecessorMessage(payload);
	       break;
	    case F_GET_PREDECESSOR_REPLY:
	       m=new GetPredecessorReply(payload);
	       break;

	    case F_FIND_SUCCESSOR:
	       m=new FindSuccessorMessage(payload);
	       break;
	    case F_FIND_SUCCESSOR_REPLY:
	       m=new FindSuccessorReply(payload);
	       break;

	    case F_GET_SUCCESSORLIST:
	       m=new GetSuccessorListMessage(payload);
	       break;
	    case F_GET_SUCCESSORLIST_REPLY:
	       m=new GetSuccessorListReply(payload);
	       break;

	    case F_ROUTED_APPLICATION: 
	       m=new RoutedApplicationMessage(payload);
	       break;
	    case F_APPLICATION:
	       m=new DirectApplicationMessage(payload);
	       break;
	    case F_APPLICATION_REPLY:
	       m=new ApplicationReply(payload);
	       break;

	    default:
	       // don't do anything, just throw away the packet
	       m=null;
	       assert false : "Unknown packet type "+func;
	 }

	 return m;
      }

      /**
	@param buf A sliced buffer.
      */
      public void writeMessage(ByteBuffer buf)
      {	
	 /* write the message */
	 write(buf);

	 /* then update the payload length field. 
	    payload length = message length - type 
	    and length fields */

	 int length=buf.position()-5;
	 if(length>0) buf.putInt(1,length);

	 if(System.currentTimeMillis()-creationTime>500) 
	    System.out.println("Message "+this+" stayed "+(System.currentTimeMillis()-creationTime)+" millis in queue.");
      }

      /**
	 Write the packet header to buf.
      */
      protected void write(ByteBuffer buf)
      {
	 byte func=0;
	 if(this instanceof PingMessage)
	    func=F_PING;
	 else if(this instanceof PingReply)
	    func=F_PING_REPLY;

	 else if(this instanceof GetPredecessorMessage)
	    func=F_GET_PREDECESSOR;
	 else if(this instanceof GetPredecessorReply)
	    func=F_GET_PREDECESSOR_REPLY;

	 else if(this instanceof SetPredecessorMessage)
	    func=F_SET_PREDECESSOR;
	 else if(this instanceof SetPredecessorReply)
	    func=F_SET_PREDECESSOR_REPLY;

	 else if(this instanceof GetSuccessorListMessage)
	    func=F_GET_SUCCESSORLIST;
	 else if(this instanceof GetSuccessorListReply)
	    func=F_GET_SUCCESSORLIST_REPLY;

	 else if(this instanceof FindSuccessorMessage)
	    func=F_FIND_SUCCESSOR;
	 else if(this instanceof FindSuccessorReply)
	    func=F_FIND_SUCCESSOR_REPLY;

	 else if(this instanceof RoutedApplicationMessage)
	    func=F_ROUTED_APPLICATION;
	 else if(this instanceof DirectApplicationMessage)
	    func=F_APPLICATION;
	 else if(this instanceof ApplicationReply)
	    func=F_APPLICATION_REPLY;

	 buf.put(func);
         // the payload length, insert 0x00000000 for padding.
	 buf.putInt((int)0); 
	 buf.putShort(sequenceNumber);
	 source.write(buf);
      }      

      /* UNIT TESTS */
/*
      public static void main(String argv[]) throws Exception
      {
	 final EndPoint source=new EndPoint(0,new byte[]{0,0,0,0});
	 final EndPoint a=new EndPoint(8888,InetAddress.getLocalHost().getAddress());
	 final EndPoint b=new EndPoint(8890,new byte[]{127,0,0,2});

	 new Thread() {
	   public void run() {
	      try{
		 System.err.println("----------------------------------------------");
		 
		 ServerSocketChannel ssc=ServerSocketChannel.open();		 
		 ssc.configureBlocking(true);
		 ServerSocket ss=ssc.socket();
		 ss.bind(new InetSocketAddress(8888));
		 SocketChannel in=ssc.accept();
		 in.configureBlocking(true);
		 
		 ByteBuffer buf=ByteBuffer.allocate(1024);				 

		 Message m=Message.readMessage(in, buf);
		 assert (m instanceof PingMessage && m.source.equals(source))
		    : "Ping message did not transfer correctly";
		 buf.clear();
		 
		 System.err.println("PASSED: edu.ucr.cs.chord.PingMessage");
		 
		 m=Message.readMessage(in, buf);
		 assert (m instanceof PingReply && m.source.equals(source))
		    : "PingReply did not transfer correctly";
		 buf.clear();

		 System.err.println("PASSED: edu.ucr.cs.chord.PingReply");

		 m=Message.readMessage(in, buf);
		 assert (m instanceof GetSuccessorListReply && 
			 m.source.equals(source) && 
			 ((GetSuccessorListReply)m).successors[0].equals(a) &&
			 ((GetSuccessorListReply)m).successors[1].equals(b))
		    : "GetSuccessorListReply did not transfer correctly";
		 buf.clear();
		 System.err.println("PASSED: edu.ucr.cs.chord.GetSuccessorListReply");

		 m=Message.readMessage(in, buf);
		 assert (m instanceof GetPredecessorReply && 
			 m.source.equals(source) && 
			 ((GetPredecessorReply)m).predecessor.equals(b))
		    : "GetPredecessorReply did not transfer correctly";
		 buf.clear();

		 System.err.println("PASSED: edu.ucr.cs.chord.GetPredecessorReply");

		 m=Message.readMessage(in, buf);
		 byte[] thisId=new HashValue(a).getBytes();
		 assert m instanceof FindSuccessorMessage;
		 byte[] thatId=((FindSuccessorMessage)m).getID();
		 boolean equals=thisId.length==thatId.length;
		 for(int i=0;equals && i<thisId.length;i++)
		    equals=equals && thisId[i]==thatId[i];
		 assert equals : "FindSuccessorId not transmitted correctly";
		 buf.clear();

		 System.err.println("PASSED: edu.ucr.cs.chord.Message");

		 System.err.println("----------------------------------------------");
	      }catch(Exception e) { e.printStackTrace(); }
	   }}.start();

	 Thread.sleep(1000);

	 new Thread() {
	   public void run() {
	      try{
		 SocketChannel out=SocketChannel.open(new InetSocketAddress("localhost",8888));

		 ByteBuffer buf=ByteBuffer.allocate(1024);
		 
		 Message ping=new PingMessage();
		 ping.writeMessage(buf);		
		 buf.flip();
		 out.write(buf);		 
		 buf.clear();		 

		 new PingReply(ping).writeMessage(buf);
		 buf.flip();
		 out.write(buf);
		 buf.clear();

		 new GetSuccessorListReply(ping,new EndPoint[] {a,b}).writeMessage(buf);
		 buf.flip();
		 System.err.println("Successor buflimit: "+buf.limit());
		 out.write(buf);
		 buf.clear();	

		 new GetPredecessorReply(ping,b).writeMessage(buf);
		 buf.flip();
		 out.write(buf);
		 buf.clear();

		 new FindSuccessorMessage(new HashValue(a).getBytes()).writeMessage(buf);
		 buf.flip();
		 out.write(buf);
		 buf.clear();		 

		 Thread.sleep(1000);

		 // TODO: add unit tests for remaining message types.

	      }catch(Exception e) { e.printStackTrace(); }
	   }}.start();       		     
      }
*/
      public String toString()
      {
	 return getClass().getName()+" "+sequenceNumber+" from: "+source;
      }
}







