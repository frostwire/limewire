package edu.ucr.cs.chord.filelookup;

import com.bitzi.util.Base32;
import com.sun.java.util.collections.*;
import edu.ucr.cs.chord.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.security.*;
import java.util.StringTokenizer;

/**
   Extends the LocalChordNode to provide FileLookup functionality. Basically, it adds
   three major function calls: add, and get. The FileLookupTable is a 
   1-M mapping of filenames to host/port pairs.
   <p>Major issues left to deal with:
<ul>
   <li> Expiry date on mappings - current mappings persist forever
   <li> Incremental backups - current backup scheme sends complete copy of mappings to successor.
</ul>
<p>
   Incremental backups give another problem though. Right now, we can set a timer on all backup
   copies and throw out anything that expires. If we start sending updates only when stuff changes,
   we won't know it has expired and need to add more complexity to the protocol...

   A nice compromise might be to not have timers and no incremental backups. Instead, nodes only
   send backup update if any _local_ info has changed.
*/
public class LocalFileLookupNode implements FileLookup, ChordListener
{
      /* the number of predecessors we keep backups for */
      public static final int BACKUP_STORES_LIMIT=3;

      /* time between backups to successor, in millis */
      public static final int BACKUP_REPLICATION_INTERVAL=10000;
      /** 
	  Maps Strings->Collections of Strings.
	  The values are either Strings or Collections of Strings.
       */
      MultipleStringMap store; 

      TreeMap /*of MultipleStringMap*/ backupStores;

      /** 
	  We don't want two people modifying the datastore at the same time. 
          Especially when we're doing transfers. Anyone doing non-atomic
	  operations on the store are required to aquire the lock first.
      */
      private Object storeLock=new Object();       

      LocalChordNode node;
      Thread backupThread;

      /** Keeps track of any changes being made since the last backup */
      boolean changedSinceBackup=false;

      /**
	 This constructor can only be called once PER CHORD.
	 Nodes created using this method cannot connect to prior nodes, which
	 will result in permanently disjoint Chord rings. 

	 Use this constructor with extreme caution.
      */
      public LocalFileLookupNode(int localPort) throws IOException
      {
	 System.out.println("InetAddress.getLocalHost(): "+InetAddress.getLocalHost()+" Tools.getLocalHost(): "+Tools.getLocalHost());
	 node=new LocalChordNode(new EndPoint(localPort, Tools.getLocalHost().getAddress())); 

	 System.out.println("WARNING! Started new, disjoint Chord ring. Are you sure this is what you intended?");

	 init();
      }

      public LocalFileLookupNode(int localPort, int port, String host) throws IOException
      {
	 System.out.println("InetAddress.getLocalHost(): "+InetAddress.getLocalHost()+" Tools.getLocalHost(): "+Tools.getLocalHost());

	 node=new LocalChordNode(new EndPoint(localPort, Tools.getLocalHost().getAddress()), 
				 new EndPoint(port, InetAddress.getByName(host).getAddress()));

	 init();

	 // request a transfer from the successor node;
	 ChordNode successor=node.getSuccessor();

	 // send the transfer request
	 ApplicationReply reply=successor.routeApplicationMessage(new DirectApplicationMessage(makePayload(new TransferMessage(node.getID()))));
	 
	 // parse the reply payload
	 TransferReply transferReply=(TransferReply)LookupMessage.readMessage(ByteBuffer.wrap(reply.getPayload()));	 

	 // set whatever MultipleStringMap the reply contains as our current datastore
	 store=transferReply.getPayload();
      }

      void init()
      {
	 node.addChordListener(this);	 
	 this.store=new MultipleStringMap();
	 this.backupStores=new TreeMap(new HashValueComparator(node.getID()));

	 this.backupThread=new Thread(new BackupController());
	 this.backupThread.start();	
      }
      
      /** 
	  Checks the store for any matching entries. If there aren't any,
	  it checks the local backups just to be sure, in case the predecessor just died.
      */	  
      String[] localGet(String urn) 
      {
	 String[] result;
	 synchronized(storeLock) {
	    result=store.getAll(urn);
	 }

	 if(result==null) {
	    System.out.println("No results, checking backups:");

	    // TODO: this can be done smarter. Check against key first etc. Or we could just trust the
	    // store, assuming that failover is so quick that this will never happen.

	    Iterator iter=backupStores.keySet().iterator();
	    while(iter.hasNext()) {
	       result=((MultipleStringMap)backupStores.get(iter.next())).getAll(urn);
	       if(result!=null && result.length>0)
		  return result;
	    }
	    return new String[0];
	 }
	 else{
	    System.out.println("Found matching result:"+result[0].toString());
	    return result;
	 }
      } 
      
      void localAdd(String urn, String uri) 
      {
	 synchronized(storeLock) {
	    store.add(urn,uri);
//	    System.out.println("Added: "+urn+"->"+uri);
//	    System.out.println("Store content now: "+store);
	    changedSinceBackup=true;
	 }
      }
        
      private byte[] getURNBytes(String urn)
      {
	 String urnString=urn.toString();
	 urnString=urnString.substring("urn:sha1:".length());
	 return Base32.decode(urnString);	 
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

	 ByteBuffer buf=ByteBuffer.allocate(m.estimateBufferSize());
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

      public ApplicationReply applicationMessageReceived(ApplicationMessage msg) 
      {
	 LookupMessage lmsg=LookupMessage.readMessage(ByteBuffer.wrap(msg.getPayload()));
	 LookupMessage reply=null;
	 if(lmsg instanceof GetMessage)
	    reply=new GetReply(localGet(((GetMessage)lmsg).urn));
	 else if(lmsg instanceof AddMessage) {
	    localAdd(((AddMessage)lmsg).urn,((AddMessage)lmsg).uri);
	    reply=null;
	 }
	 else if(lmsg instanceof TransferMessage) {

//	    System.out.println("Got TransferMessage from node: "+((TransferMessage)lmsg).getRequestorID());
	    // TODO: check to make sure the sender is the predecessor. This is a security hole before that is done.

	    MultipleStringMap payload=transfer(((TransferMessage)lmsg).getRequestorID());
//	    System.out.println("Transferring: "+payload);
	    reply=new TransferReply(payload);
	 } 
	 else if(lmsg instanceof BackupMessage) {
	    fileBackupMap(((BackupMessage)lmsg).getOwnerID(), ((BackupMessage)lmsg).getPayload());
	 }
	 return new ApplicationReply((edu.ucr.cs.chord.ApplicationMessage)msg,makePayload(reply)); 
      }
      
      public void newPredecessor(ChordNode oldPredecessor, ChordNode newPredecessor)
      {
	 if(oldPredecessor==null) return;

	 // oops, we just lost a predecessor! Now salvage all backups between the new predecessor and us.
	 if(!newPredecessor.getID().inBetweenRightExclusive(oldPredecessor.getID(),node.getID()))
	 {
	    System.out.println("Examining backups for emergency restore: "+backupStores);
	    Iterator iter=backupStores.keySet().iterator();
	    while(iter.hasNext())
	    {
	       HashValue id=(HashValue)iter.next();
	       if(id.inBetweenLeftExclusive(newPredecessor.getID(), node.getID()))
	       {
		  System.out.println("Restoring entries up to "+id+" from backups.");
		  store.putAll((MultipleStringMap)backupStores.get(id));
		  iter.remove();
	       }
	    }
	 }
	 else 
	 {
	    // the new predecessor is going to want responsibility for part of our range.
	    // however, a new predecessor must be a recently joined predecessor, which means it
	    // just sent a transfer request, so we don't do anything here.
	 }
      }

      public void newSuccessor(ChordNode oldSuccessor, ChordNode newSuccessor)
      {
	 System.out.println("Sending backup info to new successor");
	 changedSinceBackup=true;
	 if(oldSuccessor==null)
	    sendBackups(newSuccessor);
      }

      /**
	 Sends the local data, and all but the last of the backups to the successor 
      */      
      void sendBackups(ChordNode successor) 
      {
	 // TODO: this is controversial. Uncomment this line to get more bandwidth efficiency
	 // at the cost of less backup reliability. Should really do incremental even.
	 if(!changedSinceBackup) return;

	 try {
	    successor.routeApplicationMessage(new DirectApplicationMessage(makePayload(new BackupMessage(node.getID(),(MultipleStringMap)store.clone()))));
	    Map.Entry entries[]=(Map.Entry[]) backupStores.entrySet().toArray(new Map.Entry[0]);
	    int sentCount=1;
	    for(int i=0;i<entries.length;i++) {
	       HashValue backupID=(HashValue)entries[i].getKey();
	       
	       // don't send the last backupMap
	       // TODO: This could be done in asynchronous mode, but the underlying chord doesn't support it yet.
	       if(sentCount<=BACKUP_STORES_LIMIT) 
	       {
		  // don't send updates of some node to itself
		  if(!successor.getID().equals(backupID))
		  {
		     MultipleStringMap map=(MultipleStringMap)entries[i].getValue();
		     
		     if(map!=null) {
			successor.routeApplicationMessage(new DirectApplicationMessage(makePayload(new BackupMessage(backupID,((MultipleStringMap)map.clone())))));		     
			sentCount++;
		     }
		  }
	       }
	    }
	 }catch(Exception e) {
	    e.printStackTrace();
	    System.err.println("Backup transmission to "+successor+" failed:"+e);
	 } 
      }

      /**
	 Stores the given map together with the given id. Important to note is that
	 this method removes from the store any backups that are more distant than the 
	 given id. This means backups must be filed in near->far order to ensure consistency.

	 TODO: Make backups incremental. The current scheme is too bandwidth intensive.
       */
      void fileBackupMap(HashValue id, MultipleStringMap map)
      {
	 // keep backups of the BACKUP_STORES_LIMIT nodes before this node.
//	 System.out.println("Filing backup of host: "+id+": "+map);
	 if(map==null) return;
	 if(id.equals(node.getID())) return;

	 /*
	   First remove all backups that are farther away than the given id. This is
	   to get rid of any old backups that are no longer relevant due to a shrinking
	   Chord ring. If the predecessor is working correctly, it will send the relevant
	   backup maps after this id anyway.
	 */
	 Iterator iter=backupStores.tailMap(id).keySet().iterator();
	 while(iter.hasNext()) {
	    iter.next();
	    iter.remove();
	 }

	 // now add the new backup to the store.
	 backupStores.put(id,map);

	 while(backupStores.size()>BACKUP_STORES_LIMIT)
	    backupStores.remove(backupStores.lastKey());

//	 System.out.println("Backups: "+backupStores);
      }

      MultipleStringMap transfer(HashValue id)
      {	
  	 MultipleStringMap result=new MultipleStringMap();


         /** 
	     Save transferred key-value mappings in the backup table in case the joining host fails.
	     This also makes sure we can answer lookup requests while the new node is in transition.
	     NOTE: Do it here to always have the key available somewhere, primary or backup table.
	 */
	 fileBackupMap(id, result);

 	 /** 
 	     TODO: If the host has lots of mappings, this will be
 	     costly. May prefer to keep DataStore as a SortedMap 
 	     instead of the current Map. 

 	     If we're using a SortedMap, finding the id and grabbing everything below should be
 	     the best thing to do, that way we don't have to compare so many HashValues.
 	 */

 	 synchronized(storeLock)
 	 {
 	    Iterator iter=store.keySet().iterator();
 	    while(iter.hasNext())
 	    {
 	       String urn=(String)iter.next();

	       String urnString=urn.toString();
	       urnString=urnString.substring("urn:sha1:".length());
 	       HashValue keyId=HashValue.parseValue(urnString);
	       
 	       /* A key falls below id if it is not between id and nodeId */
 	       if(!keyId.inBetween(id,node.getID()))
 	       {
 		  result.addAll(urn,store.getAll(urn));

		  /** Remove the urn-uri mappings transferred from our local table. */
		  iter.remove();
 	       }
 	    }
 	 }	 	 

  	 return result;
      }

//      static String printRFD(String rfd)
//      {
//	 return  rfd.getHost()+":"+rfd.getPort()+":"+rfd.getFileName()+":"+rfd.getIndex()+":"+rfd.getSize()+":"+rfd.getSpeed();	 
//      }

      public void statusDump()
      {
	 synchronized(storeLock) {
	    System.err.println("Store size: "+store.size());
	 }
      }

      public static void main(String argv[]) throws Exception
      {
	 int port=8888;
	 LocalFileLookupNode node;
	 if(argv.length==2) {
	    port=Integer.parseInt(argv[0]);
	    node=new LocalFileLookupNode(port, Integer.parseInt(argv[1].substring(argv[1].indexOf(':')+1)), argv[1].substring(0,argv[1].indexOf(':')));
	 }
	 else {	    	    
	    node=new LocalFileLookupNode(port);
	 }
      }

      class BackupController implements Runnable
      {
	    public void run() 
	    {
	       while(true) {
		  try{
		     if(!node.getSuccessor().equals(node))
			sendBackups(node.getSuccessor());
		     Thread.sleep(BACKUP_REPLICATION_INTERVAL);
		  }catch(Exception e) {
		     System.out.println("Exception in BackupController: "+e);
		  }		  
	       }
	    }
      }
}

