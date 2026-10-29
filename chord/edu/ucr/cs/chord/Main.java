package edu.ucr.cs.chord;

import java.net.*;
import java.math.*;

/**
   Used for testing. Starts a Chord nodes and connects then, depending on command line arguments.
*/

public class Main
{
      public static void main(String argv[]) throws Exception
      {
	 LocalChordNode node;

	 byte[] localAddress=InetAddress.getLocalHost().getAddress();
	 if(argv.length==0)
	    node=new LocalChordNode(new EndPoint(8888,localAddress));
	 else
	    node=new LocalChordNode(new EndPoint(Integer.parseInt(argv[0]),InetAddress.getLocalHost().getAddress()),
				    new EndPoint(8888,InetAddress.getLocalHost().getAddress()));
	 


	 node.addChordListener(new ChordListener() {
		  public ApplicationReply applicationMessageReceived(ApplicationMessage msg) {
		     System.out.println("Message received: "+new String(msg.getPayload()));
		     return new ApplicationReply(msg,new byte[0]);
		  }
		  public void newPredecessor(ChordNode a, ChordNode b) {}
		  public void newSuccessor(ChordNode a, ChordNode b) {}
	    });
	    

	 for(int i=0;argv.length==0 && 1<10;i++) {
	    Thread.sleep(4000);	

	    HashValue id=node.getID();

	    // U G L Y  hack!!!
	    id.value=id.value.subtract(BigInteger.ONE);
	    byte bytes[]=id.getBytes();
	    id.value=id.value.add(BigInteger.ONE);

	    node.fingerTable[0].getNode().routeApplicationMessage(new RoutedApplicationMessage(bytes,"Hello World!".getBytes()));    
	    
	 }    
  
    }

}
