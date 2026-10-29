package edu.ucr.cs.chord;

import java.util.*;
import java.net.*;

/**
   An index into the Chord. Finger entries are kept at increasing intervals for the whole Chord ring, distributed like 2^x, where x=0..159.  
   Finger entries are stored in the finger table of the LocalChordNode to provide on average O(log N) message lookups in the Chord.   

   @author Jakob Eriksson
*/
class FingerEntry
{
      /** The intended start of the finger */
      HashValue start;

      /** Interface to the remote node */
      ChordNode node;

      public FingerEntry(HashValue start, ChordNode node) 
      {
	 this.node=node;
	 this.start=start;	 	 	 
      }

      public FingerEntry(HashValue start)
      {
	 this.start=start;
      }      
      
      /** 
	  Returns an interface to the fingered node 
       */
      public ChordNode getNode()
      {
	 return node;
      }
      
      /** Updates the node in this finger entry */
      public void setNode(ChordNode node)
      {
	 this.node=node;
      }

      /** 
	  Returns the actual start of this finger. When finding 
	  predecessor, this is the method to use, not getStart().
       */
      public HashValue getNodeID()
      {
	 return node.getID();
      }
      
      /** 
	  Returns the intended start of this finger. This is used
	  for reference when updating finger tables.
      */
      public HashValue getStart()
      {
	 return start;
      }

      public String toString()
      {
	 return ""+start+" "+node;
	 //return node.toString();
      }
}







