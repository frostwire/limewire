package edu.ucr.cs.chord;

import java.nio.*;

class GetPredecessorMessage extends Message {
      public GetPredecessorMessage() {}
      public GetPredecessorMessage(ByteBuffer payload) {	 
	 super(payload);
      }
}
