package edu.ucr.cs.chord;

import java.security.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
   This is the interface ChordNodes use to talk to each other. 
   Implementations are free to choose how to implement remote
   function calls.

   @author Jakob Eriksson
*/

public abstract class ChordNode
{      
      protected EndPoint host;
      HashValue nodeID;

      protected ChordNode(EndPoint host)
      {
	 this.host=host;
	 this.nodeID=new HashValue(host);
      }

      public EndPoint getHost()
      {
	 return host;
      }

      public HashValue getID()
      {
	 return nodeID;
      }
      
      public String toString()
      {
	 // return ""+getID();
	 return ""+host+" alive:"+isAlive();
      }

      public boolean equals(Object o)
      {
	 if(o instanceof ChordNode) {
	    ChordNode other=(ChordNode) o;
	    return other.getHost().equals(getHost());	    
	 }
	 else return false;	 
      }

      /*
	Need to implement this since we're changing equals, according to hashCode contract
      */
      public int hashCode()
      {
	 return host.hashCode();
      }

      /**
	 Finds the node just after the target id. Guaranteed to
	 hold if the successor pointers are consistent.
      */
      public abstract ChordNode findSuccessor(HashValue id);

      /**
	 Delivers msg at the successor of the indicated target ID. A one-shot alternative to looking up
	 the successor and connecting out-of-band. Only efficient if we're sending ONE message, otherwise
	 an out of band connection is much more efficient once we know the address.
      */
      public abstract ApplicationReply routeApplicationMessage(ApplicationMessage msg);

      /**
	 Does nothing except check if the node responds to calls. 
	 Execution without exception means all is well.
      */
      abstract void ping() throws IOException, TimeoutException;

      /**
	 Returns an array of successors of this node. <p>
	 
	 Each node periodically queries its successor for a list of successors. 
	 When queried, it returns that list, plus its successor, cropped to
	 some maximal size. The idea is that after a few steps, a correct successor
	 list is obtained with only one message per node.
      */
      abstract ChordNode[] getSuccessors() throws IOException, TimeoutException;

      /**
	 Returns the immediate predecessor of this node.
      */
      abstract ChordNode getPredecessor() throws IOException, TimeoutException;
    
      /**
	 Sets the immediate predecessor of this node. 
      */
      abstract EndPoint[] setPredecessor(ChordNode node) throws IOException, TimeoutException;

      public abstract boolean isAlive();
}

/** TODO: May want to add some more info in these, such as host and port? */
class ChordInitializationException extends ChordException
{
      public ChordInitializationException(String message)
      {
	 super("Failed to initialize Chord node, reason: "+message);
      }
}

class ChordInconsistencyException extends ChordException
{
      public ChordInconsistencyException(String message)
      {
	 super(message);
      }
}








