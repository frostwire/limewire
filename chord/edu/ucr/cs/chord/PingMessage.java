package edu.ucr.cs.chord;

import java.nio.*;

class PingMessage extends Message {
      public PingMessage() {}
      public PingMessage(ByteBuffer payload) {	 
	 super(payload);
      }
}

