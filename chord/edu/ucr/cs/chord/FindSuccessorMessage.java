package edu.ucr.cs.chord;

import java.nio.*;

class FindSuccessorMessage extends Message {
      byte[] id;
      public FindSuccessorMessage(byte[] id) {
	 this.id=id;
      }

      public FindSuccessorMessage(ByteBuffer payload) {	 
	 super(payload);
	 id=new byte[20];
	 payload.get(id);
      }
      
      public byte[] getID() {
	 return id;
      }

      protected void write(ByteBuffer buf) {
	 super.write(buf);
	 assert id.length<=20;
	 // pad with zeros if neccessary.
	 if(id.length<20)
	 {
	    for(int i=0;i<(20-id.length);i++)
	       buf.put((byte)0);
	 }

	 buf.put(id);
      }
}

