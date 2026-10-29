package edu.ucr.cs.chord;

import java.nio.*;

/**
   Application messages are delivered to the successor of the given ID through
   Cord routing.   
*/
public class RoutedApplicationMessage extends Message implements ApplicationMessage {
      byte[] applicationPayload;
      byte[] targetID;

      public RoutedApplicationMessage(byte[] ID, byte[] applicationPayload) 
      {
	 this.targetID=ID;
	 this.applicationPayload=applicationPayload;
      }

      public RoutedApplicationMessage(ByteBuffer payload) {	 
	 super(payload);

	 targetID=new byte[20];
	 payload.get(targetID);

	 // whatever remains when headers have been read is the
	 // application payload.
	 applicationPayload=new byte[payload.remaining()];
	 payload.get(applicationPayload);
      }

      public byte[] getID()
      {
	 return targetID;
      }

      public void setID(byte[] ID)
      {
	 this.targetID=ID;
      }

      public byte[] getPayload()
      {
	 return applicationPayload;
      }

      public int estimateBufferSize() {
	 return super.estimateBufferSize()+applicationPayload.length+20;
      }

      public void write(ByteBuffer buf)
      {
	 super.write(buf);
	 buf.put(targetID);
	 buf.put(applicationPayload);
      }
}

