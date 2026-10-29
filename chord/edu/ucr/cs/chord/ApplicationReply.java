package edu.ucr.cs.chord;

import java.nio.*;

/**
   A reply to an ApplicationMessage. Contains a payload that will be delivered to the source of the ApplicationMessage.
*/

public class ApplicationReply extends Reply {
      byte[] applicationPayload;
      public ApplicationReply(ApplicationMessage inResponseTo, byte[] applicationPayload) {
	 super((Message)inResponseTo);
	 this.applicationPayload=applicationPayload;
      }     
      public ApplicationReply(ByteBuffer payload) {
	 super(payload);
	 applicationPayload=new byte[payload.remaining()];
	 payload.get(applicationPayload);
      }
      public void write(ByteBuffer buf)
      {
	 super.write(buf);
	 buf.put(applicationPayload);
      }

      public int estimateBufferSize() {
	 return super.estimateBufferSize()+applicationPayload.length;
      }

      public byte[] getPayload()	 
      {
	 return applicationPayload;
      }      
}


