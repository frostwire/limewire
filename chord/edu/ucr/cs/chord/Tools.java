package edu.ucr.cs.chord;

import java.net.*;
import java.io.*;

public class Tools
{
      /** 
	  Incredibly ugly trick to get the true localhost address. 
      */
      public static InetAddress getLocalHost() {
	 int bogusport=30000;
	 while(true) try{
	    InetAddress addr=null;
	    Socket bogus=new Socket("www.cnn.com",80);
	    addr=bogus.getLocalAddress();
	    bogus.close();
	    return addr;
	 }catch(IOException e) {
	    System.out.println("Bogus port: "+bogusport+" taken, trying another.");
	 }
      }     
}
