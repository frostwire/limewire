package edu.ucr.cs.chord;

import java.security.*;
import java.util.*;
import java.net.*;
import java.math.*;
import com.bitzi.util.Base32;

/** 
    Indicate a position on the Chord ring. Provides some operations for HashValue comparison and manipulation.

    @author Jakob Eriksson
    @author Alan Jackson
*/

public class HashValue
{      
      BigInteger value;          

      /* Keep small during testing for output readability */
      public static int HASH_SIZE=160;  
      public static BigInteger MAX_VALUE=BigInteger.ONE.shiftLeft(HASH_SIZE);

      public HashValue(EndPoint host)
      {
	 try{
	    MessageDigest digest = MessageDigest.getInstance("SHA-1");

	    byte[] bytes=host.toString().getBytes();
	    digest.update(bytes);

//	    assert bytes.length==20;

	    // this means we get positive AND negative numbers, do we care?
	    value=new BigInteger(1,digest.digest());

	 }catch (NoSuchAlgorithmException e) {
	    assert false:"SHA-1 digest not available!";
	 }
      }
      
      private HashValue(BigInteger value)
      {
	 this.value=value;
      }                

      /**
	 Creates a new HashValue. The new HashValue=this.value+2^fingerNumber
      */
      public HashValue createFingerValue(int fingerNumber)
      {
	 assert fingerNumber<HASH_SIZE; // 0->HASH_SIZE-1, right?

	 /* 2<<fingerNumber = 2^fingerNumber . The & is the same as modulo in this case. */
	 BigInteger newValue=value.add(BigInteger.ONE.shiftLeft(fingerNumber)).mod(MAX_VALUE);
	 return new HashValue(newValue);
      }

      /** @param hashValue in Base32 form */
      public static HashValue parseValue(String hashValue)
      {	 
	 return new HashValue(new BigInteger(Base32.decode(hashValue)));
      }

      public static HashValue create(byte[] hashValueBytes)
      {
	 return new HashValue(new BigInteger(hashValueBytes));
      }

      /** 
	  Tells if this value is between v1 and v2.
	  Left and right inclusive.
      */
      public boolean inBetween(HashValue v1, HashValue v2)
      {
	 BigInteger value1 = v1.value,
	    value2 = v2.value;
	 
	 if (value1.compareTo(value2)<0)
	    return (value.compareTo(value1) >= 0 && value.compareTo(value2) <= 0);
	 else
	    return (value.compareTo(value1) >= 0 || value.compareTo(value2) <= 0);	 
      }

      public boolean inBetweenLeftExclusive(HashValue v1, HashValue v2)
      {
	 BigInteger value1 = v1.value,
	    value2 = v2.value;
	 
	 if (value1.compareTo(value2)<0)
	    return (value.compareTo(value1) > 0 && value.compareTo(value2) <= 0);
	 else
	    return (value.compareTo(value1) >= 0 || value.compareTo(value2) < 0);	 
      }

      public boolean inBetweenRightExclusive(HashValue v1, HashValue v2)
      {
	 BigInteger value1 = v1.value,
	    value2 = v2.value;
	 
	 if (value1.compareTo(value2)<0)
	    return (value.compareTo(value1) >= 0 && value.compareTo(value2) < 0);
	 else
	    return (value.compareTo(value1) > 0 || value.compareTo(value2) <= 0);	 
      }

      public int compareTo(HashValue other)
      {
	 return value.compareTo(other.value);
      }

      public boolean equals(Object o)
      {
	 return (o instanceof HashValue) && ((HashValue)o).value.equals(value);	    
      }

      public int hashCode()
      {
	 return value.hashCode();
      }

      /**
	 Borrowed this code from the mozilla security/jss project.
	 http://www.mozilla.org
      */
      public byte[] getBytes()
      {
	 assert value.compareTo(BigInteger.valueOf(0)) != -1 : "Negative HashValue!";

	 byte[] bytes;

	 int bitlen = value.bitLength();
	 int bytelen;
	 if( bitlen == 0)
	    bytelen = 1;
	 else 
	    bytelen = (bitlen + 7)/8;

	 byte[] withSign=value.toByteArray();
	 
	 if(bytelen == withSign.length)
	    bytes=withSign;
	 else {
	    assert bytelen == withSign.length - 1;
	    assert withSign[0] == 0;
	    byte[] without = new byte[bytelen];
	    System.arraycopy(withSign, 1, without, 0, bytelen);

	    bytes=without;
	 }

	 assert bytes.length == 20 : "Bad hash size: "+bytes.length+" bitlength: "+value.bitLength()+" top byte: "+bytes[bytes.length-1]+" bottom bytes: "+bytes[1]+" "+bytes[0];
	 return bytes;
      }

      public String toString()
      {
	 return Base32.encode(value.toByteArray());
      }

      /** 
	  Unit tests only
      */
      public static void main(String argv[]) throws Exception
      {
	 HashValue fifteen=new HashValue(new BigInteger("15"));

	 /* regular tests */

	 assert fifteen.inBetween(new HashValue(new BigInteger("15")), new HashValue(new BigInteger("20")));
	 assert !fifteen.inBetween(new HashValue(new BigInteger("16")), new HashValue(new BigInteger("20")));

	 assert !fifteen.inBetweenRightExclusive(new HashValue(new BigInteger("10")), new HashValue(new BigInteger("15")));
	 assert !fifteen.inBetweenLeftExclusive(new HashValue(new BigInteger("15")), new HashValue(new BigInteger("20")));

	 HashValue a=HashValue.parseValue("AC3GY4J6GUCVCMW6VPYHEJDE2TXX7DOL");
	 HashValue b=HashValue.parseValue("AC6KVVVJXCSRRALIC7UHJ45GWGT2H762WE");
	 HashValue c=HashValue.parseValue("APMSI6CP3BUOGPACKANQJV2HB7M2JYSW");
	    
	 assert a.inBetweenRightExclusive(b, c);
	 assert a.inBetween(b, c);

	 HashValue d=HashValue.parseValue("AC2KTCJ7SN7XIPPNTF4GTUZWZQKBJR7J");
	 HashValue e=HashValue.parseValue("I7LICJ35TIX52U3UUO7OHPRF6QOYYBS7");
	 HashValue f=HashValue.parseValue("AC3LVCX4FTGQYNNQ5RJFRTNCFCOFDQKIHU");

	 assert e.inBetweenRightExclusive(d, f);

	 HashValue g=HashValue.parseValue("AC2KTCJ7SN7XIPPNTF4GTUZWZQKBJR7J");
	 HashValue h=HashValue.parseValue("AC3LVCX4FTGQYNNQ5RJFRTNCFCOFDQKIHU");
	 HashValue i=HashValue.parseValue("APMSI6CP3BUOGPACKANQJV2HB7M2JYSW");

	 assert g.inBetweenRightExclusive(h,i);
	 assert g.inBetween(h,i);

// AC2KTCJ7SN7XIPPNTF4GTUZWZQKBJR7JRQ is not inBetween AC3LVCX4FTGQYNNQ5RJFRTNCFCOFDQKIHU and APMSI6CP3BUOGPACKANQJV2HB7M2JYSW
// AC2KTCJ7SN7XIPPNTF4GTUZWZQKBJR7J is inBetweenRightEx AC3LVCX4FTGQYNNQ5RJFRTNCFCOFDQKIHU and APMSI6CP3BUOGPACKANQJV2HB7M2JYSW

	 System.err.println("PASSED: HashValue");
      }
}

class HashValueException extends Exception
{
      public HashValueException(String message)
      {
	 super(message);
      }
}
