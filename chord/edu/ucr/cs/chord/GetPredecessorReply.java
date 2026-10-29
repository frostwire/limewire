package edu.ucr.cs.chord;

import java.nio.*;

/**
   | Predecessor Port | Predecessor IP |
          2 bytes          4 bytes
*/

class GetPredecessorReply extends Reply {
      EndPoint predecessor;
      public GetPredecessorReply(Message inResponseTo, EndPoint predecessor) {
	 super(inResponseTo);
	 this.predecessor=predecessor;
      }

      public GetPredecessorReply(ByteBuffer payload) {	 
	 super(payload);
	 predecessor=new EndPoint(payload);
      }

      protected void write(ByteBuffer buf) {
	 super.write(buf);
	 predecessor.write(buf);
      }

      public EndPoint getPredecessor() {
	 return predecessor;
      }
}

