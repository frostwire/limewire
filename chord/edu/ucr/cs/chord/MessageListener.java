package edu.ucr.cs.chord;

/**
   Implemented by classes who want message event notification from a Connector.
*/

public interface MessageListener
{
      /**
	 Called when a non-reply message is received. Or a reply which wasn't caught by an action in the actiontable. 
      */
      public void messageReceived(Message msg);
      
      /**
	 Called when messages asynchronously fail to be delivered due to I/O errors. 
      */
      public void messageDeliveryFailed(Message msg);
}
