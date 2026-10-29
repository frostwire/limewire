package edu.ucr.cs.chord;

import java.nio.*;

class SetPredecessorMessage extends Message {
      EndPoint predecessor;
      public SetPredecessorMessage(EndPoint predecessor) {
	 this.predecessor=predecessor;
      }

      public SetPredecessorMessage(ByteBuffer payload) {	 
	 super(payload);
	 predecessor=new EndPoint(payload);
      }

      protected void write(ByteBuffer buf) {
	 super.write(buf);
	 predecessor.write(buf);
      }

      public EndPoint getPredecessor()
      {
	 return predecessor;
      }
}

