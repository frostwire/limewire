package edu.ucr.cs.chord;

import java.nio.*;

class FindSuccessorReply extends Reply {
      EndPoint successor;
      public FindSuccessorReply(Message inResponseTo, EndPoint successor) {
	 super(inResponseTo);
	 this.successor=successor;
      }

      public FindSuccessorReply(ByteBuffer payload) {	 
	 super(payload);
	 successor=new EndPoint(payload);
      }

      protected void write(ByteBuffer buf) {
	 super.write(buf);
	 successor.write(buf);
      }

      public EndPoint getSuccessor() {
	 return successor;
      }
}
