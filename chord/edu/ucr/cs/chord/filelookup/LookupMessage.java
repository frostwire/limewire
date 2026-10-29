package edu.ucr.cs.chord.filelookup;

import com.limegroup.gnutella.*;
import edu.ucr.cs.chord.*;
import java.security.*;
import com.sun.java.util.collections.*;
import java.util.StringTokenizer;
import java.net.*;
import java.nio.*;
import com.bitzi.util.Base32;
import java.util.Set.*;

abstract class LookupMessage
{
      public static final byte F_GET=0x00;
      public static final byte F_GET_REPLY=0x08;
      public static final byte F_ADD=0x01;
      public static final byte F_TRANSFER=0x02;
      public static final byte F_TRANSFER_REPLY=0x0A;

      public static final byte F_BACKUP=0x03;

      public static LookupMessage readMessage(ByteBuffer message)
      {
	 byte func=message.get();

	 switch(func) {
	    case F_GET:
	       return new GetMessage(message);
	    case F_GET_REPLY:
	       return new GetReply(message);
	    case F_ADD:
	       return new AddMessage(message);
	    case F_TRANSFER:
	       return new TransferMessage(message);
	    case F_TRANSFER_REPLY:
	       return new TransferReply(message);
	    case F_BACKUP:
	       return new BackupMessage(message);
	    default:
	       return null;
	 }
      }

      public abstract void write(ByteBuffer buf);
      
      public int estimateBufferSize() {
	 return 1024; // this is naive.
      }
}



/**
   Requests a set of Strings corresponding to the given URN.

   |Type|URN|
     1   20
*/

class GetMessage extends LookupMessage
{
      String urn;

      public GetMessage(String urn)
      {
	 this.urn=urn;
      }

      protected GetMessage(ByteBuffer message) 
      {
	 byte[] bytes=new byte[message.remaining()];
	 message.get(bytes);

	 //TODO: may want to do some fancy decoding here.
	 urn=new String(bytes);
      }

      public void write(ByteBuffer buf) 
      {
	 buf.put(F_GET);
	 //TODO: may want to do some fancy encoding here.
	 buf.put(urn.getBytes());
      }      
}

/**
   Contains a list of Strings that correspond to the URN in the request.

  |Type|ResultCount| 
    1        2
   |URI-length|URI ...
        1      
    ...
*/

class GetReply extends LookupMessage
{
      private String uris[];

      public GetReply(String uris[])
      {
	 this.uris=uris;
      }

      protected GetReply(ByteBuffer message) 
      {
	 int resultCount=(int)(message.getShort() & 0xffff); // unsign it
	 uris=new String[resultCount];

	 for(int i=0;i<resultCount;i++) {	    
	    int length=(int)(message.get() & 0xff); // unsign it

	    byte[] bytes=new byte[length];
	    message.get(bytes);
	    uris[i]=new String(bytes);
	 }
      }

      public String[] getURIs()
      {
	 return uris;
      }

      public void write(ByteBuffer buf) 
      {
	 buf.put(F_GET_REPLY);
	    
	 buf.putShort((short)uris.length);
	 for(int i=0;i<uris.length;i++)
	 {
	    String uri=uris[i];
	    buf.put((byte)uri.length());
	    buf.put(uri.getBytes());
	 }
      }
}

/**
   The AddMessage adds a mapping from the given URN to the given URI

   |Type|URNLength|URNString|URILength|URIString
     1       1         ?        1          ?
*/

class AddMessage extends LookupMessage
{
      String urn;
      String uri;

      public AddMessage(String urn, String uri)
      {
	 this.urn=urn;
	 this.uri=uri;
      }

      protected AddMessage(ByteBuffer message)
      {	 
	 int urnLength=(int)(message.get() & 0xff); // unsigned
	 byte[] urnBytes=new byte[urnLength];
	 message.get(urnBytes);
	 urn=new String(urnBytes);

	 int uriLength=(int)(message.get() & 0xff); // unsigned
	 byte[] uriBytes=new byte[uriLength];
	 message.get(uriBytes);
	 uri=new String(uriBytes);
      }
  
      public void write(ByteBuffer buf) 
      {
	 buf.put(F_ADD);

	 buf.put((byte)urn.length());
	 buf.put(urn.getBytes());

	 buf.put((byte)uri.length());
	 buf.put(uri.getBytes());
      }
}

/**
   A TransferMessage requests all key-value pairs in the range [id, id of recipient]

   |Type|ID|
     1   20
*/

class TransferMessage extends LookupMessage
{
      HashValue id;
      public TransferMessage(HashValue id)
      {
	 this.id=id;	 
      }

      protected TransferMessage(ByteBuffer message)
      {
	 byte[] idBytes=new byte[20];
	 message.get(idBytes);
	 this.id=HashValue.create(idBytes);
      }

      public void write(ByteBuffer buf)
      {
	 buf.put(F_TRANSFER);

	 buf.put(id.getBytes());
      }

      public HashValue getRequestorID() 
      {
	 return id;
      }
}

/**
   The TransferReply contains a list of mappings that used to be on the
   sending node, but should now reside on the receiving node.

   |Type|URNCount|URNLength|URNString|URICount|URILength|URIString
     1      2         1         ?         1        1         ?
                                               URILength|URIString
					           1         ?
		     ...
*/    
class TransferReply extends LookupMessage
{
      MultipleStringMap payload;
      public TransferReply(MultipleStringMap payload)
      {
	 this.payload=payload;
      }

      public MultipleStringMap getPayload() {
	 return payload;
      }
      
      public int estimateBufferSize() {
	 return 1024+255*payload.size();
      }
      
      protected TransferReply(ByteBuffer message)
      {
	 int URNCount=(int)(message.getShort() & 0xffff); //unsigned
	 payload=new MultipleStringMap();

	 for(int i=0;i<URNCount;i++) 
	 {	    
	    // Get the URN
	    int urnLength=(int)(message.get() & 0xff); //unsigned
	    byte[] urnBytes=new byte[urnLength];
	    message.get(urnBytes);
	    String urn=new String(urnBytes);

	    // Now get the URIs
	    int uriCount=(int)(message.getShort() & 0xffff); //unsigned	       
	    for(int j=0;j<uriCount;j++) 
	    {	       
	       int uriLength=(int)(message.get() & 0xff); //unsigned 
	       byte[] uriBytes=new byte[uriLength];
	       message.get(uriBytes);
	       String uri=new String(uriBytes);	       
	       payload.add(urn, uri);
	    }
	 }
      }

      public void write(ByteBuffer buf) 
      {
	 buf.put(F_TRANSFER_REPLY);

	 /* write urncount */
	 buf.putShort((short)payload.size());
	 Iterator urns=payload.keySet().iterator();
	 while(urns.hasNext()) 
	 {
	    String urn=(String)urns.next();

	    assert urn.length()<255 : "Maximum URN length is 255 characters";
	    buf.put((byte)urn.length());
	    buf.put(urn.getBytes());

	    /* write uri count */
	    String[] uris=payload.getAll(urn);
	    buf.putShort((short)uris.length);
	   
	    /* write the port, host, index, size and name */
	    for(int i=0;i<uris.length;i++) 
	    {
	       String uri=uris[i];
	       buf.put((byte)uri.length());
	       buf.put(uri.getBytes());	       
	    }	    
	 }
      }
}

/**
   The BackupMessage requests that the receiving node store the
   included mappings for backup purposes. If the sending node fails 
   at a future date, the receiving node retains a copy of its mappings. 

   BackupMessages are sent by nodes to their successor. In most cases, they
   will also send all the backup copies they have, except for the last one,
   that way implementing multiple redundant backups.
*/

/*
   |Type|OwnerID|URNCount|URNLength|URNString|URICount|URILength|URIString
     1      20      2         1         ?        1         1         ?
                                                      |URILength|URIString
						           1         ?
							   ...
                         |URNLength|URNString|URICount|URILength|URIString
                              1         ?        1         1         ?
			    ...
*/    
class BackupMessage extends LookupMessage
{
      MultipleStringMap payload;
      HashValue ownerid;

      public BackupMessage(HashValue ownerid, MultipleStringMap payload)
      {
	 this.payload=payload;
	 this.ownerid=ownerid;
      }
      
      public MultipleStringMap getPayload() {
	 return payload;
      }

      public HashValue getOwnerID() {
	 return ownerid;
      }

      public int estimateBufferSize() {
	 // TODO: make a more reliable estimation.
	 int size=1024+(payload.size()<<8);
//	 System.out.println("Backup message size limit: "+(1024+2*payload.getByteSize())+" old value:"+size);
	 return 1024+2*payload.getByteSize(); // this is way over the what's necessary, but it's a hassle to compute.
      }
      
      protected BackupMessage(ByteBuffer message)
      {
	 byte[] ownerBytes=new byte[20];
	 message.get(ownerBytes);
	 ownerid=HashValue.create(ownerBytes);

	 int URNCount=(int)(message.getShort() & 0xffff); //unsigned
	 payload=new MultipleStringMap();

	 try{
	    for(int i=0;i<URNCount;i++) 
	    {	    
	       // Get the URN
	       int urnLength=(int)(message.get() & 0xff); //unsigned
	       byte[] urnBytes=new byte[urnLength];
	       message.get(urnBytes);
	       String urn=new String(urnBytes);
	       
	       // Now get the URIs
	       int uriCount=(int)(message.getShort() & 0xffff); //unsigned	       
	       for(int j=0;j<uriCount;j++) 
	       {	       
		  int uriLength=(int)(message.get() & 0xff); //unsigned 
		  byte[] uriBytes=new byte[uriLength];
		  message.get(uriBytes);
		  String uri=new String(uriBytes);	       
		  payload.add(urn, uri);
	       }
	    }
	 }catch(BufferUnderflowException e) {
	    System.out.println("WARNING: Recoverable error: Buffer underflow in BackupMessage.");
	 }
      }

      public void write(ByteBuffer buf) 
      {
	 buf.put(F_BACKUP);
	 /* owner */
	 buf.put(ownerid.getBytes());

	 synchronized(payload) {
	    /* write urncount */
	    buf.putShort((short)payload.size());
	    Iterator urns=payload.keySet().iterator();
	    while(urns.hasNext()) 
	    {
	       String urn=(String)urns.next();

	       assert urn.length()<255 : "Maximum URN length is 255 characters";
	       buf.put((byte)urn.length());
	       buf.put(urn.getBytes());

	       /* write uri count */
	       String[] uris=payload.getAll(urn);
	       buf.putShort((short)uris.length);
	   
	       /* write the port, host, index, size and name */
	       for(int i=0;i<uris.length;i++) 
	       {
		  String uri=uris[i];
		  buf.put((byte)uri.length());
		  buf.put(uri.getBytes());	       
	       }	    
	    }
	 }
      }
}


/*
      public static void main(String argv[]) throws Exception
      {
	 // GetMessage Unit Test

	 URN urn=URN.createSHA1Urn("urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB");
	 URN urn2=URN.createSHA1Urn("urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFG");
	 GetMessage getmsg=new GetMessage(urn);
         ByteBuffer buf=ByteBuffer.allocate(1024);
	 getmsg.write(buf);
	 buf.flip();
	 GetMessage getmsg2=(GetMessage)readMessage(buf);
	 buf.clear();
	 
	 Assert.that(getmsg.urn.equals(getmsg2.urn),"GetMessage\tFAILED");
	 System.out.println("GetMessage\tPASSED");
	 
	 // GetReply Unit Test

	 String rfd1=createString(88,"127.0.0.1",2345,"MyFile.txt",654,urn);
	 String rfd2=createString(99,"127.0.0.2",2532,"MyFile2.txt",644,urn);
	 String rfd3=createString(99,"127.1.0.2",2332,"MyFile4.txt",674,urn);
	 GetReply getreply=new GetReply(new String[] {rfd1,rfd2});	 	    
	 
	 getreply.write(buf);
	 buf.flip();
	 GetReply getreply2=(GetReply)readMessage(buf);
	 buf.clear();
	 
	 String[] uris=getreply2.getUris(urn);

	 Assert.that(uris[0].equals(rfd1) &&
		     uris[1].equals(rfd2), 
		     "GetReply\tFAILED");
	 System.out.println("GetReply\tPASSED");

	 // AddMessage Unit Test

	 AddMessage addmsg=new AddMessage(urn,rfd1);
         
	 addmsg.write(buf);
	 buf.flip();
	 AddMessage addmsg2=(AddMessage)readMessage(buf);
	 buf.clear();
	 
	 Assert.that(addmsg.urn.equals(addmsg2.urn) &&
		     addmsg.file.equals(addmsg2.file),"AddMessage\tFAILED : "+addmsg2.urn+"-"+addmsg2.file);
	 System.out.println("AddMessage\tPASSED");

	 // TransferMessage Unit Test

	 TransferMessage tmsg=new TransferMessage(HashValue.parseValue("PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB"));
	 tmsg.write(buf);
	 buf.flip();
	 TransferMessage tmsg2=(TransferMessage)readMessage(buf);
	 buf.clear();
	 
	 Assert.that(tmsg.id.equals(tmsg2.id), "TransferMessage\tFAILED");
	 System.out.println("TransferMessage\tPASSED");

	 // TransferReply Unit Test

	 StringMap map=new MultipleStringMap();
	 map.add(urn,rfd1);
	 map.add(urn,rfd2);
	 map.add(urn2,rfd3);
	 TransferReply treply=new TransferReply(map);	 	    
	 treply.write(buf);
	 buf.flip();
	 TransferReply treply2=(TransferReply)readMessage(buf);
	 buf.clear();
	 	 
	 Assert.that(treply.payload.equals(treply2.payload),"TransferReply\tFAILED");
	 System.out.println("TransferReply\tPASSED (conditional, values in RFDMap not checked)");	 

	 // TODO: add unit test for BackupMessage
      }


      public static RemoteFileDesc createRemoteFileDesc(int port, String address, long index, String fileName, long size, URN urn)
      {
	 Set urns=new HashSet();
	 if(urn!=null)
	    urns.add(urn);
	       
	 return new RemoteFileDesc(address,
				   port,
				   index,
				   fileName,
				   (int)size,
				   null,  // clientguid
				   SpeedConstants.MODEM_SPEED_INT,  // TODO: perhaps include this too in message?
				   false, // chat  TODO: figure this one out
				   2,     // quality TODO: any other suggestions?
				   false, // browse TODO: figure this one out
				   null,
				   urns);				  
      }


*/
