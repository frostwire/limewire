package edu.ucr.cs.chord.filelookup;

import java.net.*;
import com.limegroup.gnutella.*;

public class TestLookup
{
      public static void main(String argv[]) throws Exception
      {
	 LocalFileLookupNode node=new LocalFileLookupNode(9999, 8888,InetAddress.getLocalHost().getHostAddress());
	 
	 String urn="urn:sha1:ELSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB";
	 node.add(urn, "http://www.cs.ucr.edu/~jeriksson");
	 System.out.println(node.get(urn)[0]);	 
      }
}

