package edu.ucr.cs.chord;

import java.security.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.math.*;

/**
   A local implementation of the ChordNode interface.

   The major things that remain are:
   
   <ul>
   <li> <b>Tamper/sabotage resistance features</b>. [Sit and Morris, 2002]
   <li> Chord consistency monitoring
   <li> Support for caching in the application layer. Probably by indicating "incoming connection" for each message.
   </ul>

   @author Jakob Eriksson
*/
public class LocalChordNode extends ChordNode implements MessageListener
{
      /** 
	  Fast index into the chord. Keeps track of nodes 2^N steps away, 
	  where N is the number of bits in the HashValue representation.

	  if fingerTable[0].getNode()==this
      */
      FingerEntry[] fingerTable;  

      /**
	 The successor list handles node failures by keeping track of
	 the following successors too. If the successor fails, we fall
	 back to the next one.

	 The size of the successor list should optimally be O(log N), but
	 since we don't know N we'll set it to the number of unique hosts
	 in the finger table.
      */
      LinkedList successors;
      
      ChordNode predecessor;

      Connector connector;


      /** Maintains reliable successor pointers */
      Stabilizer stabilizer;

      /** Maintains effective fingers */
      FingerFixer fingerFixer;

      ChordListener listener;

      /** For record keeping purposes only, 
	  do not .stop() or .interrupt() these threads. */
      Thread stabilizerThread;
      Thread fingerFixerThread;

      /** Millis between checking for new/dead successors */
      static final int STABILIZER_INTERVAL=3000; 

      /**
	 Millis between a random finger updates.
      */
      static final int FINGERFIXING_INTERVAL=10000; 

      /** 
	  Default constructor. Sets up a brand new chord ring.
      */
      public LocalChordNode(EndPoint localHost) throws UnknownHostException, IOException, TimeoutException
      {
	 super(localHost);

	 initialize();	    	   
	 
	 setSuccessor(this);

	 startThreads();
      }
      
      /** 
	  Connects this new chordnode into an existing chord.
	  @param chordHost A host on the chord ring.
      */
      public LocalChordNode(EndPoint localHost, EndPoint remoteHost) throws TimeoutException, IOException, UnknownHostException
      {	 
	 super(localHost);
	 
	 initialize();

	 ChordNode accessPoint=RemoteChordNode.createNode(remoteHost, this, false);
	 ChordNode successor=accessPoint.findSuccessor(getID());

	 setSuccessor(successor);

	 startThreads();
      }

      /** 
	  Only handles one listener so far
       */
      public void addChordListener(ChordListener listener)
      {
	 this.listener=listener;
      }
      
      public ApplicationReply applicationMessageReceived(ApplicationMessage message)
      {
	 if(listener!=null)
	    return listener.applicationMessageReceived(message);
	 else
	    return new ApplicationReply(message, new byte[0]);
      }

      /** 
	  Initializes some variables.
	  @param port The port this ChordNode will listen to for connections.
      */
      protected void initialize() throws IOException
      {
	 this.fingerTable=new FingerEntry[HashValue.HASH_SIZE];

	 /* initialize the finger table */
	 for(int i=0;i<fingerTable.length;i++) {
	    HashValue fingerStart=nodeID.createFingerValue(i);
	    fingerTable[i]=new FingerEntry(fingerStart);
	 }	   

	 this.successors=new LinkedList();
	 connector=new Connector(getHost());
	 connector.setMessageListener(this);
      }

      /**
	 Starts up the socket socket listener thread and the 
	 fingertable stabilization thread.
      */
      private void startThreads() 
      {
	 stabilizer=new Stabilizer();
	 stabilizerThread=new Thread(stabilizer);
	 stabilizerThread.start();

	 fingerFixer=new FingerFixer();
	 fingerFixerThread=new Thread(fingerFixer);
	 fingerFixerThread.start();
      }

      /**
	 Stops the ChordServer and the Stabilizer for this node.
	 Node failure mechanisms should take care of any problems
	 at other nodes.
      */
      public void stop()
      {
	 stabilizer.stop();
      }

      public ApplicationReply routeApplicationMessage(ApplicationMessage msg) 
      {
	 // First check if the message has any routing information, else 
	 // process it at the local node.

	 if(msg instanceof DirectApplicationMessage)
 	    return applicationMessageReceived(msg);
	 
	 RoutedApplicationMessage message=(RoutedApplicationMessage)msg;
	 HashValue id=HashValue.create(message.getID());
	 
	 if(id.equals(getID()))
	 {
	    return applicationMessageReceived(message);
	 }
	 else 
	 {
	    ChordNode successor=getSuccessor();
	    if(id.inBetweenRightExclusive(nodeID, successor.getID()))
	    {
	       // update the target id to exactly match that of the successor.
	       // this is necessary because the successor can't know if it really
	       // is the successor, only the directly preceding node knows that for
	       // sure.

	       // make sure the message gets through to some successor or other.
	       ApplicationReply r=null;
	       message.setID(successor.getID().getBytes());
	       r=successor.routeApplicationMessage(message);
	       assert r!=null:"Was unable to send application message";
	       return r;
	    }
	    else {	       
	       ChordNode closest=closestPrecedingFinger(id);
	       return closest.routeApplicationMessage(message);
	    }
	 }
      }    


      /** 
	  TODO: Update this to return 1 or more successors. This is very useful
	  for locality improved Chord fingers. A locality improved finger would 
	  pick the successor with the least latency.
      */
      public ChordNode findSuccessor(HashValue id) 
      {
	 ChordNode result=localFindSuccessor(id);
	 if(result!=null)
	    return result;
	 else {
	    ChordNode closest=closestPrecedingFinger(id);
	    return closest.findSuccessor(id);
	 }
      }

      ChordNode localFindSuccessor(HashValue id) 
      {
	 ChordNode successor=getSuccessor();
	 if(id.inBetweenRightExclusive(nodeID, successor.getID()))
	    return successor;
	 else
	    return null;
      }

      ChordNode closestPrecedingFinger(HashValue id) 
      {
	 /* If this is the only node in the chord, 
	    there is no closest preceding finger */
	 for(int i=HashValue.HASH_SIZE-1;i>=1;i--)
	 {
	    ChordNode finger=fingerTable[i].getNode();

	    if(finger==null || !finger.isAlive()) continue;
	    
	    /* 
	       If we've got a finger that's between us and the target,
	       then use it! Traversing the list backwards should make
	       us take the longest possible jump.
	       
	       Note that it doesn't matter for consistency if the finger
	       table is correct or not, only for speed.

	       Note that this is a left exclusive comparison, the finger has to
	       fall after getID() and before or on id.	       
	    */
	    
	    if(!finger.equals(this) && 
	       finger.getID().inBetween(getID(),id))
	       return fingerTable[i].getNode();
	 }
	 
	 // if there aren't any working fingers, we resort to the successor.
	 // the successor pointer is the only thing that has to be correct
	 // for lookup to work.
	 return getSuccessor();
      }
     
      public ChordNode getSuccessor() 
      {	 
	 if(!fingerTable[0].getNode().isAlive()) {
	    System.err.println("Successor has failed: "+fingerTable[0].getNode()+", handling failure");
	    handleSuccessorFailure();
	 }

	 return fingerTable[0].getNode();
      }

      private void handleSuccessorFailure() 
      {
	 synchronized(successors)
	 {
	    System.err.println("Available successors for failover are: "+successors);
	    Iterator i=successors.iterator();
	    while(i.hasNext())
	    {
	       try{
		  ChordNode node=(ChordNode)i.next();
		  // remove dead successors
		  if(!node.isAlive())
		     i.remove();
		  else {
		     // then pick the first live one. 
		     // setSuccessor() notifies the successor of the change
		     // let's not put ourselves in the finger table.
		     System.err.println("Successor failover to: "+node.getHost());

		     // ping it just to make sure it's alive. If it's not, it'll at least be failed next time around.
		     node.ping();
		     setSuccessor(node);
		     return;
		  }
	       }catch(Exception e)
	       { 
		  System.err.println("FAILOVER ERROR: "+e.getMessage()); 
	       }
	    }
	 }
	 
	 /* if, worst case, we can't find any more successors, then go back to singleton mode.
	    this is a really bad situation in most cases, but what can you do? 
	    TODO: turn off Chord and start looking for other nodes again.
	 */
	 try{
	    setSuccessor(this);
	    System.err.println("FATAL ERROR: Chord stability failure. Singleton mode.");
	 }catch(Exception e)
	 {
	    System.err.println("ERROR: Couldn't fall back to singleton mode. Reason: "+e.getMessage());
	 }
      }

      ChordNode[] getSuccessors() 
      {
	 synchronized(successors) {
	    return (ChordNode[])successors.toArray(new ChordNode[successors.size()]);	    
	 }
      }	    
      
      ChordNode getPredecessor() 
      {	
	 if(predecessor==null) 
	    return this;   
	 else 
	    return predecessor;
      }      

      /**
	 Tries to set node as predecessor to this node. Returns 0 if it worked, and -1 if not.
	 Setting predecessor pointers is (or should be) restricted to the actual predecessor
	 on the circle, as verified by port and address.
      */
      EndPoint[] setPredecessor(ChordNode node) 
      {	 
	 ChordNode oldPredecessor=predecessor;
 	 predecessor=node;
	 
	 // debug output
	 System.err.println("PREDECESSOR: "+predecessor.getHost());	 
	 
	 if(listener!=null)
	    listener.newPredecessor(oldPredecessor, predecessor);
	 
	 return getFingers();
      }

      protected void setSuccessor(ChordNode node) throws IOException, TimeoutException
      { 	 
	 if(!node.equals(this) && !connector.socketPrepared(node.getHost()))
	    connector.prepareSocket(node.getHost());

	 // update predecessor pointer on the successor.
	 // if the new successor doesn't answer, the exception will 
	 // make sure we don't update our successor.

	 EndPoint[] fingers=node.setPredecessor(this);
	 spliceFingers(fingers);

	 ChordNode oldSuccessor=fingerTable[0].getNode();
	 fingerTable[0].setNode(node);	 

	 if(listener!=null)
	    listener.newSuccessor(oldSuccessor, node);

	 System.err.println("SUCCESSOR: "+node.getHost());
      }

      public boolean isAlive() { return true; }
      void ping() {}

      /**
	 Returns a list of unique non-null fingers in the fingertable.
	 Fingers are returned in order of increasing distance from the local node.
      */	 
      public EndPoint[] getFingers() {

	 if(fingerTable[0].getNode()==null)
	    return new EndPoint[0];

	 LinkedList endPoints=new LinkedList();
	 EndPoint last=fingerTable[0].getNode().getHost();
	 endPoints.add(last);

	 for(int i=0;i<fingerTable.length;i++)
	 {
	    if(fingerTable[i]!=null && fingerTable[i].getNode()!=null)
	    {
	       EndPoint current=fingerTable[i].getNode().getHost();
	       if(!last.equals(current))
	       { 
		  endPoints.add(current);
		  last=current;
	       }
	    } 
	 }

	 return (EndPoint[])endPoints.toArray(new EndPoint[endPoints.size()]);
      }

      /**
	 Inserts the given fingers into the local finger table. The fingers are generally
	 retreived from the successor of the current node.

	 @param fingers is assumed to be in order of increasing distance from local node.
      */
      void spliceFingers(EndPoint[] fingers)
      {
	 for(int i=0;i<fingers.length;i++) {
	    
	    HashValue hash=new HashValue(fingers[i]);
	    int fingerIndex=hash.value.bitLength()-1;
	    
	    if(fingerTable[fingerIndex].getNode()==null ||
	       !fingerTable[fingerIndex].getNodeID().inBetween(this.getID(),hash))
	       fingerTable[fingerIndex].setNode(RemoteChordNode.createNode(fingers[i], this,true));
	 }

//	 statusDump();
      }

      /** 
	  Calculates the optimal no of successors to keep track of. According to the
	  Chord paper, the number of successors should be log2(N) for maximum redundancy, which
	  is the approx same as the number of unique fingers.

	  TODO: May want to bring this down to more reasonable levels some time. Perhaps log2(N)
	  is a bit excessive. Better safe than sorry though... always keep 3 successors, even if
	  we have less than 8 nodes in the Chord.
      */
      public int maxSuccessorCount() { 
	 int uniqueFingers=0;
	 // TODO: Cache/optimize this.
	 ChordNode last=fingerTable[0].getNode();
	 for(int i=1;i<HashValue.HASH_SIZE;i++) {
	    if(fingerTable[i].getNode()!=null && !last.equals(fingerTable[i].getNode())) {
	       last=fingerTable[i].getNode();
	       uniqueFingers++;
	    }
	 }
	 return Math.max(3,uniqueFingers); 
      }


      /** 
	  Calculate the lowest finger that needs updating. Since the whole space is 160 bits we have
	  160 fingers, which is usually rather excessive. fixFingers optimizes this by not fixing fingers
	  that are below the highest finger that is equal to the successor. 
	  
	  Let me try to explain that one more time: at any given time, in any given network all the lower 
	  order fingers are likely to point to the successor of the local node. Above the first finger that
	  doesn't point to the successor, they are likely to all point at different nodes. This means that
	  as long as we poll the highest successor-pointing finger now and then, we're going to catch all
	  updates that concern us without polling the lower order fingers.
      */
      public int getFingerFixingLimit() 
      {
	 // TODO: cache this result
	 // TODO: is this calculation completely correct? I think I may be off by one in the extreme cases.
	 ChordNode successor=getSuccessor();
	 int limit;
	 for(limit=2;limit<HashValue.HASH_SIZE;limit++)
	 {
	    ChordNode node=fingerTable[limit].getNode();
	    if(node!=null && !node.equals(successor)) 
	       return limit-1;
	 }
	 
	 return HashValue.HASH_SIZE-2; // one for the array index starting at 0, and one for the extra finger.
      }
	    
      public void statusDump()
      {
	 System.err.println("Status dump for host: "+getHost());
	 System.err.println("-F-I-N-G-E-R---T-A-B-L-E-----------------------------");
	 ChordNode lastPrinted=null;
	 for(int i=0;i<fingerTable.length;i++) {
	    if(i==0 || (fingerTable[i].getNode()!=null && !fingerTable[i].getNode().equals(lastPrinted))) {	       
	       System.err.println(i+" "+fingerTable[i]);
	       lastPrinted=fingerTable[i].getNode();
	    }
	 }
	 System.err.println("-----------------------------------------------------");
	 if(predecessor!=null)
	    System.err.println("Predecessor: "+predecessor.getHost());

	 synchronized(successors) {
	    System.err.println(successors);
	 }

	 System.err.println(connector);
      }

      public static void main(String argv[]) throws Exception
      {
	 LocalChordNode node=new LocalChordNode(new EndPoint(8888,Tools.getLocalHost().getAddress()));

	 node.addChordListener(new ChordListener() {
		  public ApplicationReply applicationMessageReceived(ApplicationMessage msg) { 
		     System.err.println("Message received: "+new String(msg.getPayload())); 
		     return new ApplicationReply(msg,new byte[0]); 
		  }
	          public void newSuccessor(ChordNode a, ChordNode b) {}
	          public void newPredecessor(ChordNode a, ChordNode b) {}});	

	 HashValue id=node.getID();
	 id.value=id.value.subtract(BigInteger.ONE);
	 
	 node.routeApplicationMessage(new RoutedApplicationMessage(id.getBytes(),"Hello World!".getBytes()));
      }
      
      /**
	 Handles node failure gracefully.
      */
      public void messageDeliveryFailed(Message msg)
      {
	 msg.failureCount++;

	 System.err.println("Message delivery failed: "+msg);

	 connector.reportFailure(msg.getNextDestination());

	 if(msg instanceof FindSuccessorMessage) {	    
	    
	    if(msg.failureCount>RemoteChordNode.MAX_RETRIES) {
	       assert false : "Unable to route FindSuccessorMessage: "+msg+" Chord consistency in jeopardy.";
	       return;
	    }

	    // any dead nodes should've been updated by now, so it's worth another shot.
	    
	    messageReceived(msg);
	 }
	 else if(msg instanceof RoutedApplicationMessage) {
	    System.err.println("Rerouting application message.");

	    if(msg.failureCount>RemoteChordNode.MAX_RETRIES) {
	       assert false : "Unable to route RoutedApplicationMessage: "+msg+" Chord consistency in jeopardy.";
	       return;
	    }

	    // give it another shot. If we were sending this to the successor, there's no
	    // harm done from changing the id since the next successor is the successor of 
	    // the successor anyway.

	    messageReceived(msg);
	 }	 
	 else
	    System.err.println("Got message failure notification for: "+msg+" need to handle this.");
      }

      /** 
	  asynchronously handles all received messages. This could be improved to only start threads when they are needed.
       */
      public void messageReceived(Message msg) 
      {
	 new MessageHandler(msg).start();
      }

      class MessageHandler extends Thread {
	    Message msg;
	    public MessageHandler(Message msg) {
	       this.msg=msg;
	    }
	    public void run() {
	       try {
		  if(msg instanceof PingMessage)
		     connector.transmitReply(new PingReply(msg));
		  else if(msg instanceof GetSuccessorListMessage) {
		     ChordNode[] list=getSuccessors();
		     EndPoint[] successors=new EndPoint[list.length];
		     for(int i=0;i<list.length;i++)
			successors[i]=list[i].getHost();
		     connector.transmitReply(new GetSuccessorListReply(msg, successors));
		  }
		  else if(msg instanceof GetPredecessorMessage) {
		     connector.transmitReply(new GetPredecessorReply(msg, getPredecessor().getHost()));
		  }
		  else if(msg instanceof FindSuccessorMessage) {
		     HashValue id=HashValue.create(((FindSuccessorMessage)msg).getID());	       	       
		     ChordNode successor=localFindSuccessor(id);

		     if(successor!=null) {
			FindSuccessorReply reply=new FindSuccessorReply(msg, successor.getHost());
			reply.setNextDestination(msg.getSource());
			connector.transmitReply(reply);
		     }
		     /* if we don't have the successor, we send the message off to our closest finger */
		     else {
			ChordNode closest=closestPrecedingFinger(id);
			msg.setNextDestination(closest.getHost());
			connector.transmit(msg,null);
		     }
		  }
		  else if(msg instanceof SetPredecessorMessage) {
		     EndPoint[] fingers=setPredecessor(RemoteChordNode.createNode(((SetPredecessorMessage)msg).getPredecessor(),LocalChordNode.this,false));
		     connector.transmitReply(new SetPredecessorReply(msg, fingers));
		     System.out.println("Sent SetPredecessor message");
		  }
		  else if(msg instanceof ApplicationMessage) {
		     ApplicationReply reply;

		     if(msg instanceof DirectApplicationMessage)
			connector.transmitReply(applicationMessageReceived((ApplicationMessage)msg));
		     else{		  
			RoutedApplicationMessage message=(RoutedApplicationMessage)msg;
			HashValue id=HashValue.create(message.getID());
	 
			if(id.equals(getID()))
			   connector.transmitReply(applicationMessageReceived(message));
			else 
			{
			   ChordNode successor=getSuccessor();
			   if(id.inBetweenRightExclusive(nodeID, successor.getID()))
			   {
			      // update the target id to exactly match that of the successor.
			      // this is necessary because the successor can't know if it really
			      // is the successor, only the directly preceding node knows that for
			      // sure.
			
			      // make sure the message gets through to some successor or other.
			      ApplicationReply r=null;
			      try {
				 message.setID(successor.getID().getBytes());
				 message.setNextDestination(successor.getHost());
				 connector.transmit(message, null);
			      } catch(Exception e) {
				 System.err.println("Problem while sending ApplicationMessage to successor: "+e);
			      }
			   }
			   else {
			      ChordNode closest=closestPrecedingFinger(id);
			      message.setNextDestination(closest.getHost());
			      connector.transmit(message, null);
			   }
			}
		     }
		  }
		  else {
		     System.err.println("WARNING: Uncaught message! "+msg);
		  }
	       }catch(IOException e) { e.printStackTrace(); }
	    }

      }
      /**
	 The Stabilizer periodically updates a random finger. We could probably
	 make this more adaptive/efficient somehow.
      */
      class Stabilizer implements Runnable
      {	    
	    Random rand;
	    boolean keepRunning=true;

	    public Stabilizer()
	    {
	       rand=new Random(System.currentTimeMillis());
	    }
	    public void run() 
	    {
	       while(keepRunning) {
		  try{Thread.sleep(LocalChordNode.STABILIZER_INTERVAL); }catch(Exception e){}
		  stabilize();
	       }
	    }

	    /** 
		Ensure that the successor pointer is correct at all times.
		Keep track of backup successor pointers in case of node failure.
	    */
	    public void stabilize()
	    {
//	       boolean trouble=false;
	       while(true)
	       {
		  /* check if there's a node x between our successor and us.
		     If so, then that node is our true successor. */	       	       
		  ChordNode successor=null;
		  try{
		     successor=getSuccessor();		  
		     
//		     if(trouble)
//			System.err.println("FAILOVER SUCCESSOR: "+successor.getHost());
		     
		     ChordNode x=successor.getPredecessor();		    		  

		     // MAJOR TODO: Need some nice security features right here!
		     
		     if(//x.isAlive() && // in case this was a recently dropped dead successor
			!x.equals(LocalChordNode.this) && 
			!x.equals(successor)) {
			// make sure the joining node really belongs as successor to this node.
			//if(x.getID().inBetween(LocalChordNode.this.getID(),successor.getID()))
			{
			   System.err.println("Successor discovered: "+x.getHost());		     
			   setSuccessor(x);		  		     
			   successor=x;
			}
			//else 
			{
			   // if the predecessor of the successor is not between us and
			   // the successor, then it's been wrongfully assigned. Set things
			   // straight.
			   
			//   getSuccessor().setPredecessor(LocalChordNode.this);

			   // TODO: since we're getting a bunch of fingers, we might as well
			   // splice them here too, no? It's a very marginal gain though.
			}
		     }

		     /* update the backup successor list, keeping maxSuccessorCount() entries.
			
			TODO: a malicious node could return an empty or list of successors. 
			We could either get successors by lookup, or compare with old succcessorlist.
		     */		  		  		  		 
		     if(!LocalChordNode.this.equals(successor))
		     {
			ChordNode[] afterSuccessor=successor.getSuccessors();
			LinkedList newSucc=new LinkedList();
			newSucc.add(successor);
			
			for(int i=0;i<afterSuccessor.length && i<(maxSuccessorCount()-1);i++)
			   if(!successor.equals(afterSuccessor[i]))
			      newSucc.addLast(afterSuccessor[i]);
			
			successors=newSucc;
//			System.err.println("Successors: "+successors);
//			statusDump();
		     }

		     break;
		  }catch(Exception e) 
		  {
		     System.err.println("Stabilizer, successor nonresponsive: "+successor.getHost()+" : "+successor.isAlive()+" "+e);		     
//		     statusDump();
//		     trouble=true;
		  }	 
	       }
	    }
	    
	    public void stop()
	    {
	       keepRunning=false;
	    }	    
      }


      /**
	 The FingerFixer periodically updates a random finger. We could probably
	 make this more adaptive/efficient somehow.
      */
      class FingerFixer implements Runnable
      {	    
	    Random rand;
	    boolean keepRunning=true;

	    public FingerFixer()
	    {
	       rand=new Random(System.currentTimeMillis());
	    }
	    public void run() 
	    {
	       while(keepRunning) {
		  try{Thread.sleep(LocalChordNode.FINGERFIXING_INTERVAL); }catch(Exception e){}
		  if(!LocalChordNode.this.equals(getSuccessor()))
		   fixFingers();		     
	       }
	    }
	    
	    public void fixFingers()
	    {
//	       try{
		  int fixLimit=getFingerFixingLimit(); 

		  /* 
		     pick a random finger above the limit, for fixing.
		     this could probably be optimized somehow, but then again
		     random stuff is usually pretty good, and reliable.
		  */
		  int finger=fixLimit+rand.nextInt(HashValue.HASH_SIZE-fixLimit);
		  FingerEntry entry=fingerTable[finger];		  
		  
		  /* TODO: When findSuccessor is updated to send several successors, ping
		     them all and take the closest one (within reason). */
		  ChordNode node=findSuccessor(entry.getStart());
		  
		  entry.setNode(node);

		  //System.err.println("Fixing limit: "+getFingerFixingLimit());
		  //System.err.println("Max successors: "+maxSuccessorCount());

//		  statusDump();
//	       }catch(TimeoutException e) {
//		  System.err.println("Exception in FingerFixer.fixFingers on "+getHost()+": "+e.getMessage());
//	       }	       
	    }      

	    public void stop()
	    {
	       keepRunning=false;
	    }	    
      }


      
}












