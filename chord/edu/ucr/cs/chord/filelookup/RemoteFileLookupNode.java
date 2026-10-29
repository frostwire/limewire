package edu.ucr.cs.chord.filelookup;


import com.bitzi.util.Base32;
import com.limegroup.gnutella.*;
import com.sun.java.util.collections.*;
import edu.ucr.cs.chord.*;
import java.io.*;
import java.nio.*;

/**
   Use this class to connect to a FileLookupNode at a given address and port. This class is for 
   nodes who cannot host their own LocalFileLookupNode. It is not to be used by any other class in this
   package.
*/

public class RemoteFileLookupNode implements FileLookup
{
      ChordNode node;
      
      public RemoteFileLookupNode(int remotePort, String remoteHost)
      {
	 node=RemoteChordNode.createNode(new EndPoint(remotePort, remoteHost),null, true); // null creator means we have no local node.
      }
      
      public String[] get(String urn) throws IOException
      {
	 ApplicationReply appReply=node.routeApplicationMessage(new RoutedApplicationMessage(getURNBytes(urn), 
											     makePayload(new GetMessage(urn))));
	 GetReply reply=(GetReply)LookupMessage.readMessage(ByteBuffer.wrap(appReply.getPayload()));
	 return reply.getURIs();
      }
      
      private byte[] makePayload(LookupMessage m)
      {
	 if(m==null) return new byte[0];
	 
	 ByteBuffer buf=ByteBuffer.allocate(1024);
	 m.write(buf);
	 buf.flip();
	 byte[] bytes=new byte[buf.limit()];
	 buf.get(bytes);
	 return bytes;
      }
      
      public void add(String urn, String uri) throws IOException
      {
	 node.routeApplicationMessage(new RoutedApplicationMessage(getURNBytes(urn), 
								   makePayload(new AddMessage(urn,uri))));
      }
     
      private byte[] getURNBytes(String urn)
      {
	 // TODO: this piece of crap can only handle sha1 urns.

	 urn=urn.substring("urn:sha1:".length());
	 System.out.println("URNSTRING: "+urn);
	 return Base32.decode(urn);	 
      }
}
