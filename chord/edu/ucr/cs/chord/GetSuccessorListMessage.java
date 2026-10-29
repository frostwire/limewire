package edu.ucr.cs.chord;

import java.nio.*;

class GetSuccessorListMessage extends Message {
      public GetSuccessorListMessage() {}
      public GetSuccessorListMessage(ByteBuffer payload) {	 
	 super(payload);
      }
}

