package edu.ucr.cs.chord;

import java.security.*;
import com.sun.java.util.collections.*;
import java.net.*;
import java.math.*;
import com.bitzi.util.Base32;

/** 
    This implementation uses 160 bit hash values. This implementation uses the SHA-1 hashing algorithm.

    @author Jakob Eriksson
    @author Alan Jackson
*/

public class HashValueComparator implements Comparator
{      
      HashValue reference;

      public HashValueComparator(HashValue reference)
      {
	 this.reference=reference;
      }
      
      public int compare(Object aa, Object bb)
      {
	 HashValue a=(HashValue)aa;
	 HashValue b=(HashValue)bb;
	 
	 if(a.equals(b)) { 
	    return 0;
	 }
	 else if(a.inBetween(b,reference)) {
	    return -1;
	 }
	 else {
	    return 1;
	 }      
      }
}
