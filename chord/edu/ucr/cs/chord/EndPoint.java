package edu.ucr.cs.chord;

import java.nio.channels.*;
import java.nio.*;
import java.net.*;

/**
   An EndPoint is a Host / Port pair. The current implementation supports only IPv4 addresses. <p>   
   This class provides methods for reading/writing EndPoints to ByteBuffers. <p>    
*/
// TODO: May want to change this to use InetAddress instead to support IPv6.  
public class EndPoint {
      public static final int BYTE_SIZE=6;

      int port;
      byte[] host;

      public EndPoint(int port, byte[] host)
      {
	 this.port=port;
	 this.host=host;
      }

      /** It is illegal to pass this constructor a host that at some point cannot be resolved.
	  This means, only pass numeric string addresses to this constructor. */
      public EndPoint(int port, String host) 
      {	 
	 // TODO: Check for numeric string addresses.
	 try{
	    this.port=port;
	    this.host=InetAddress.getByName(host).getAddress();
	 }catch(UnknownHostException e) {
	    assert false : "EndPoint was passed an unknown host. This is a contract breach.";
	 }
      }
      
      public EndPoint(ByteBuffer buf)
      {
	 // TODO: what happens with ports>2^15-1 ?

	 port=(int)buf.getShort();
	 host=new byte[4];
	 buf.get(host);	 
      }
      
      public void write(ByteBuffer out)
      {
	 out.putShort((short)port);
	 out.put(host);
      }

      public int getPort() {
	 return port;
      }

      public byte[] getAddress() {
	 return host;
      }

      public String getHostAddress() {
	 try{
	    return InetAddress.getByAddress(host).getHostAddress();
	 }catch(Exception e){
	    assert false : "Failed to generate String from address, this is ridiculous.";
	    return null;
	 }
      }

      public InetSocketAddress getSocketAddress() 
      {
	 return new InetSocketAddress(getHostAddress(),getPort());
      }
      
      public boolean equals(Object o)
      {
	 return  (o instanceof EndPoint &&
		  ((EndPoint)o).port==this.port &&
		  ((EndPoint)o).host[0]==this.host[0] &&
		  ((EndPoint)o).host[1]==this.host[1] &&
		  ((EndPoint)o).host[2]==this.host[2] &&
		  ((EndPoint)o).host[3]==this.host[3]);
      }      

      public String toString()
      {
      	 // convert signed to unsigned
      	 int[] uHost = new int[4];
      	 for(int i = 0; i < 4; i++) {
      	    if(host[i] < 0)
      	       uHost[i] = 256 + host[i];
      	    else
      	       uHost[i] = host[i];
      	 }
	 return uHost[0]+"."+uHost[1]+"."+uHost[2]+"."+uHost[3]+":"+port;
      }

      public int hashCode() {
	 return toString().hashCode();
      }
}




