package edu.ucr.cs.chord;

/**
   Applications on top of a Chord, such as the file lookup table, implement this
   interface to get notifications of changes in the underlying Chord topology.
*/

public interface ChordListener
{
      public ApplicationReply applicationMessageReceived(ApplicationMessage msg);

      /**
	 Called when a new predecessor has been set.
      */
      public void newPredecessor(ChordNode oldPredecessor, ChordNode newPredecessor);

      /**
	 Called when a new successor has been set.
      */
      public void newSuccessor(ChordNode oldSuccessor, ChordNode newSuccessor);
}
