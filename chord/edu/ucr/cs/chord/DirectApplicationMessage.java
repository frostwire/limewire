package edu.ucr.cs.chord;

import java.nio.*;

/**
   Direct Application messages arent' routed, but delivered at the first host it reaches.   
*/
public class DirectApplicationMessage extends Message implements ApplicationMessage {
      byte[] applicationPayload;

      public DirectApplicationMessage(byte[] applicationPayload) 
      {
	 this.applicationPayload=applicationPayload;
      }

      public DirectApplicationMessage(ByteBuffer payload) {	 
	 super(payload);

	 // whatever remains when headers have been read is the
	 // application payload.
	 applicationPayload=new byte[payload.remaining()];
	 payload.get(applicationPayload);
      }

      public int estimateBufferSize() {
	 return super.estimateBufferSize()+applicationPayload.length;
      }
      
      public byte[] getPayload()
      {
	 return applicationPayload;
      }

      public void write(ByteBuffer buf)
      {
	 super.write(buf);
	 buf.put(applicationPayload);
      }
}

