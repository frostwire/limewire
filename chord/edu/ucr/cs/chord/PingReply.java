package edu.ucr.cs.chord;

import java.nio.*;

class PingReply extends Reply {
      public PingReply(Message inResponseTo) {
	 super(inResponseTo);
      }

      public PingReply(ByteBuffer payload) {	 
	 super(payload);
      }
}

