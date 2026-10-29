package edu.ucr.cs.chord;

import java.nio.*;

/**
   A Reply is a Message with an extra field indicating which Message this message is in response to.
   <p>
   Binary message format:<br>
   <table>
    <tr><td><i>Message</i></td><td>inResponseTo</td></tr>
    <tr><td> ... </td><td>2 bytes</td></tr>
   </table>
*/

class Reply extends Message
{
      // sequence number of message to which this is a reply
      short inResponseTo;
      transient Message inResponseToMsg;

      public Reply(Message inResponseTo)
      {
	 this.inResponseTo=inResponseTo.getSequenceNumber();
	 inResponseToMsg=inResponseTo;
	 setNextDestination(inResponseTo.getSource());
      }

      public Reply(ByteBuffer payload)
      {
	 super(payload);
	 this.inResponseTo=payload.getShort();
      }

      public short getInResponseTo()
      {
	 return inResponseTo;
      }

      protected void write(ByteBuffer buf)
      {
	 super.write(buf);
	 buf.putShort(inResponseTo);

	 if(System.currentTimeMillis()-inResponseToMsg.creationTime>500)
	    System.out.println("Reply to "+inResponseToMsg+" took "+(System.currentTimeMillis()-inResponseToMsg.creationTime)+" millis.");
      }
      
      public String toString()
      {
	 return super.toString()+" in response to "+inResponseTo;
      }
      
}

