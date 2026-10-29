package edu.ucr.cs.chord;

import java.nio.*;

/**
   Application messages are delivered to the successor of the given ID through
   Cord routing.   
*/
public interface ApplicationMessage {
      public byte[] getPayload();
}

