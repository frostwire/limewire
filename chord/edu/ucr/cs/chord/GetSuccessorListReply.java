package edu.ucr.cs.chord;

import java.nio.*;


/**
   | SuccessorCount | SuccPort1 | SuccIP1 | ... | SuccPortN | SuccIPN |
   |      1         |     2     |    4    | ... |     2     |    4    |
*/

class GetSuccessorListReply extends Reply {
      EndPoint[] successors;

      public GetSuccessorListReply(Message inResponseTo, EndPoint[] successors) {
	 super(inResponseTo);
	 this.successors=successors;
      }

      public GetSuccessorListReply(ByteBuffer payload) {	 
	 super(payload);

	 byte successorCount=payload.get();
	 successors=new EndPoint[successorCount];

	 for(int i=0;i<successorCount;i++)
	    successors[i]=new EndPoint(payload);	    
      }

      protected void write(ByteBuffer buf) {
	 super.write(buf);
	 buf.put((byte)successors.length);
	 for(int i=0;i<successors.length;i++)
	    successors[i].write(buf);
      }

      public EndPoint[] getSuccessors() {
	 return successors;
      }
}

