package edu.ucr.cs.chord;

import java.nio.*;

class SetPredecessorReply extends Reply {
      EndPoint[] fingers;
      public SetPredecessorReply(Message inResponseTo, EndPoint[] fingers) {
	 super(inResponseTo);
	 this.fingers=fingers;
      }

      public SetPredecessorReply(ByteBuffer payload) {	 
	 super(payload);
	 int count=(int)payload.get();
	 fingers=new EndPoint[count];
	 for(int i=0;i<count;i++)
	    fingers[i]=new EndPoint(payload);
      }
      
      protected void write(ByteBuffer buf)
      {
	 super.write(buf);
	 buf.put((byte)fingers.length);
	 for(int i=0;i<fingers.length;i++)
	    fingers[i].write(buf);
      }

      public EndPoint[] getFingers() 
      {
	 return fingers;
      }
}

