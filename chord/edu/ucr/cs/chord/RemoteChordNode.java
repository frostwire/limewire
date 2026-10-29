package edu.ucr.cs.chord;

import java.util.*;
import java.net.*;
import java.io.*;

/**
   A remote implementation of the ChordNode interface. This is just a 
   stub that sends remote method calls to a LocalChordNode somewhere on
   the network.

   @author Jakob Eriksson
*/
public class RemoteChordNode extends ChordNode
{
      public static final int MAX_RETRIES=3;

      static Map createdNodes=new HashMap();

      Connector connector;
      LocalChordNode creator;

      /** 
	  Internal constructor. Need to check for already created connections first.
      */
      protected RemoteChordNode(EndPoint host, LocalChordNode creator, boolean useTCP) 
      {	 
	 super(host);
	 this.creator=creator;
	 this.connector=creator.connector;

	 try{
	    if(useTCP)
	       connector.prepareSocket(getHost());
	 }catch(Exception e) {
	    e.printStackTrace();
	 }
      }
      
      public static ChordNode createNode(EndPoint host, LocalChordNode creator, boolean useTCP)
      {
	 if(creator!=null && creator.getHost().equals(host))
	    return creator;
	 
	 ChordNode node=(ChordNode)createdNodes.get(host);    
	 
	 // If we had a pre-existing node, but it was dead, we try to reinitialize it.
	 // TODO: What happens to the ChordWorker for the "dead" connection? Probably just an exception and dies..
//	 if(node!=null && !node.isAlive()) node.initialize();
	 
	 if(node==null)	{
	    System.err.println("Creating new RemoteChordNode at "+host);
	    node=new RemoteChordNode(host,creator,useTCP);
	    createdNodes.put(host,node);
        }
	  
	 return node;
      }

      ChordNode createNode(EndPoint host) 
      {
	 return createNode(host, creator, false);
      }
      
      public ChordNode findSuccessor(HashValue id) 
      {
	 try{
	    FindSuccessorReply r=(FindSuccessorReply)transcieve(new FindSuccessorMessage(id.getBytes()));
	    return createNode(r.getSuccessor());
	 }catch(IOException e) {	   
	    // TODO: should probably have some stoppers somewhere to prevent infinite recursion here
	    // we're betting that the next attempt will work better. Need to keep close track of socket problems!

	    e.printStackTrace(); // TODO: comment
	    System.err.println("Successor lookup encountered an error. Trying a recursive call to findSuccessor()");

	    return creator.findSuccessor(id);
	 }
      }
      
      public ApplicationReply routeApplicationMessage(ApplicationMessage m) 
      {
	 try {
	    return (ApplicationReply)transcieve((Message)m);	 
	 }catch(IOException e) {
	    // TODO: should probably have some stoppers somewhere to prevent infinite recursion here
	    // we're betting that the next attempt will work better. Need to keep close track of socket problems!

	    ((Message)m).failureCount++;
	    
	    if(((Message)m).failureCount<RemoteChordNode.MAX_RETRIES) {
//	       e.printStackTrace(); // TODO; comment
	       System.err.println("ApplicationMessage routing encountered an error. Trying a recursive call to routeApplicationMessage()");	       
	       return creator.routeApplicationMessage(m);
	    }
	    else {
	       assert false : "Recursive retries exceeded MAX_RETRIES.";
	       return null;
	    }
	 }
      }

      ChordNode[] getSuccessors() throws IOException, TimeoutException
      {
	 GetSuccessorListReply r=(GetSuccessorListReply)transcieve(new GetSuccessorListMessage());

	 EndPoint[] successors=r.getSuccessors();
	 ChordNode[] nodes=new ChordNode[successors.length];
	 for(int i=0;i<successors.length;i++)
	    nodes[i]=createNode(successors[i]);

	 return nodes;
      }

      ChordNode getPredecessor() throws IOException, TimeoutException
      {
	 GetPredecessorReply r=(GetPredecessorReply)transcieve(new GetPredecessorMessage());
	 return createNode(r.getPredecessor());
      }

      EndPoint[] setPredecessor(ChordNode node) throws IOException, TimeoutException
      {      
	 SetPredecessorReply r=(SetPredecessorReply)transcieve(new SetPredecessorMessage(node.getHost()));

	 return r.getFingers();
      }

      void ping() throws IOException, TimeoutException
      {
	 PingReply r=(PingReply)transcieve(new PingMessage());	    
      }

      Reply transcieve(Message m) throws IOException, TimeoutException
      {
	 m.setNextDestination(getHost());
	 return connector.transcieve(m);
      }

      /**
	 Called by LocalChordNode when it gets messageDeliveryFailed events. Reply handling
	 is already set up for this message, so all we do is hurl it at someone else and hope
	 for the best.	 
      */
      void youTry(Message m) 
      {
	 m.setNextDestination(getHost());
	 connector.transmit(m,null);
      }
      
      /**
	 Returns true if the node has not been retried so many times
	 that it is deemed dead. 
      */
      public boolean isAlive() 
      { 
	 // TODO: MAJOR TODO HERE! Return a more reliable answer to the question!
	 return !connector.hasFailed(getHost());
      }
}












