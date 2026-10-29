package org.tutorialdht.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.management.RuntimeErrorException;

import org.limewire.io.SimpleNetworkInstanceUtils;
import org.limewire.mojito.Context;
import org.limewire.mojito.EntityKey;
import org.limewire.mojito.KUID;
import org.limewire.mojito.MojitoDHT;
import org.limewire.mojito.MojitoFactory;
import org.limewire.mojito.db.DHTValueEntity;
import org.limewire.mojito.result.BootstrapResult;
import org.limewire.mojito.result.FindValueResult;
import org.limewire.mojito.result.BootstrapResult.ResultType;
import org.limewire.mojito.settings.BucketRefresherSettings;
import org.limewire.mojito.settings.DatabaseSettings;
import org.limewire.mojito.settings.KademliaSettings;
import org.limewire.mojito.settings.NetworkSettings;
import org.limewire.mojito.settings.RouteTableSettings;
import org.limewire.mojito.util.ContactUtils;
import org.limewire.mojito.util.MojitoUtils;

public class BookReviewsDHT {
	
    InetSocketAddress bootstrapHost = null;
    List<MojitoDHT> dhts;

	private Book [] bookReviews = {
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "1", "It was long and boring like the teacher who forced me to read it."),
		new Book("The Canterbury Tales", "Jeff Chaucer", "It was old", "2", "The teacher who forced me to read it was awful."),
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "2", "It was long and boring."),
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "3", "It was long and boring."),
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "4", "It was long and boring."),
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "5", "It was long and boring."),
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "6", "It was long and boring."),
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "7", "It was long and boring."),
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "8", "It was long and boring."),
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "9", "It was long and boring."),
		new Book("The Canterbury Tales", "Geoffrey Chaucer", "14th Century", "10", "It was long and boring."),
		
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "1", "Too many people."),
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "2", "Too many people."),
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "3", "Too many people."),
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "4", "Too many people."),
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "5", "Too many people."),
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "6", "Too many people."),
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "7", "Too many people."),
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "8", "Too many people."),
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "9", "Too many people."),
		new Book("The Count of Monte Cristo", "Alexandre Dumas", "1844", "10", "Too many people."),
		
		new Book("Exeter Book", "various", "1050", "1", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		new Book("Exeter Book", "various", "1050", "2", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		new Book("Exeter Book", "various", "1050", "3", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		new Book("Exeter Book", "various", "1050", "4", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		new Book("Exeter Book", "various", "1050", "5", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		new Book("Exeter Book", "various", "1050", "6", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		new Book("Exeter Book", "various", "1050", "7", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		new Book("Exeter Book", "various", "1050", "8", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		new Book("Exeter Book", "various", "1050", "9", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		new Book("Exeter Book", "various", "1050", "10", "Only read The Seafarer ... 'True is the tale that I tell of my travels'."),
		
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "1", "Which side of the egg are you supposed to eat first."),
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "2", "Which side of the egg are you supposed to eat first."),
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "3", "Which side of the egg are you supposed to eat first."),
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "4", "Which side of the egg are you supposed to eat first."),
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "5", "Which side of the egg are you supposed to eat first."),
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "6", "Which side of the egg are you supposed to eat first."),
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "7", "Which side of the egg are you supposed to eat first."),
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "8", "Which side of the egg are you supposed to eat first."),
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "9", "Which side of the egg are you supposed to eat first."),
		new Book("Gulliver's Travels", "Jonathan Swift", "1726", "10", "Which side of the egg are you supposed to eat first."),

		new Book("Oliver Twist", "Charles Dickens", "1838", "1", "Could have had less of the Artful Dodger."),
		new Book("Oliver Twist", "Charles Dickens", "1838", "2", "Could have had less of the Artful Dodger."),
		new Book("Oliver Twist", "Charles Dickens", "1838", "3", "Could have had less of the Artful Dodger."),
		new Book("Oliver Twist", "Charles Dickens", "1838", "4", "Could have had less of the Artful Dodger."),
		new Book("Oliver Twist", "Charles Dickens", "1838", "5", "Could have had more of the Artful Dodger."),
		new Book("Oliver Twist", "Charles Dickens", "1838", "6", "Could have had more of the Artful Dodger."),
		new Book("Oliver Twist", "Charles Dickens", "1838", "7", "Could have had more of the Artful Dodger."),
		new Book("Oliver Twist", "Charles Dickens", "1838", "8", "Could have had more of the Artful Dodger."),
		new Book("Oliver Twist", "Charles Dickens", "1838", "9", "Could have had more of the Artful Dodger."),
		new Book("Oliver Twist", "Charles Dickens", "1838", "10", "Could have had more of the Artful Dodger."),
	
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "1", "I love saying 'Quixote'."),
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "2", "I love saying 'Quixote'."),
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "3", "I love saying 'Quixote'."),
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "4", "I love saying 'Quixote'."),
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "5", "I love saying 'Quixote'."),
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "6", "I love saying 'Quixote'."),
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "7", "I love saying 'Quixote'."),
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "8", "I love saying 'Quixote'."),
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "9", "I love saying 'Quixote'."),
		new Book("Don Quixote", "Miguel de Cervantes", "1605", "10", "I love saying 'Quixote'."),

		new Book("Moby-Dick", "Herman Mellville", "1851", "1", "Maybe it was just a guy chasing a big fish?"),
		new Book("Moby-Dick", "Herman Mellville", "1851", "2", "white whale?"),
		new Book("Moby-Dick", "Herman Mellville", "1851", "3", "didn't like"),
		new Book("Moby-Dick", "Herman Mellville", "1851", "4", "not for me"),
		new Book("Moby-Dick", "Herman Mellville", "1851", "5", "read worse"),
		new Book("Moby-Dick", "Herman Mellville", "1851", "6", "Not bad"),
		new Book("Moby-Dick", "Herman Mellville", "1851", "7", "OK"),
		new Book("Moby-Dick", "Herman Mellville", "1851", "8", "OK"),
		new Book("Moby-Dick", "Herman Mellville", "1851", "9", "OK"),
		new Book("Moby-Dick", "Herman Mellville", "1851", "10", "Loved"),
	
		new Book("Adventures of Huckelberry Finn", "Mark Twain", "1884", "3", "Second half was a bit not as good."),
		new Book("Adventures of Huckelberry Finn", "Mark Twain", "1884", "4", "Nice."),
		new Book("Adventures of Huckelberry Finn", "Mark Twain", "1884", "5", "Good."),
		new Book("Adventures of Huckelberry Finn", "Mark Twain", "1884", "6", "Awesome."),
		new Book("Adventures of Huckelberry Finn", "Mark Twain", "1884", "8", "So-so."),
		new Book("Adventures of Huckelberry Finn", "Mark Twain", "1884", "7", "Blah second half."),
		new Book("Adventures of Huckelberry Finn", "Mark Twain", "1884", "9", "Fun."),
		new Book("Adventures of Huckelberry Finn", "Mark Twain", "1884", "10", "Good tale."),
		new Book("Adventures of Huckelberry Finn", "Mark Twain", "1884", "1", "Pretty good."),
		new Book("Adventures of Huckelberry Finn", "Samuel Clemens", "1884", "2", "Enjoyed it.")
		
	};
    
	public void closeDHT() {
		for(MojitoDHT dht : dhts) {
			dht.close();
		}
	}

	private void dhtSettings() {
        //Allows for running locally       
        ContactUtils.setNetworkInstanceUtils(new SimpleNetworkInstanceUtils(false));        

        //Allows for running locally       
        org.limewire.mojito.util.ContactUtils.setNetworkInstanceUtils(new SimpleNetworkInstanceUtils(false));        
        RouteTableSettings.MAX_CONTACTS_PER_NETWORK_CLASS_RATIO.setValue(1.0f);
        org.limewire.mojito.settings.NetworkSettings.LOCAL_IS_PRIVATE.setValue(false);
        NetworkSettings.FILTER_CLASS_C.setValue(false);

		DatabaseSettings.MAX_VALUES_PER_ADDRESS.set(Integer.MAX_VALUE - 1);

		DatabaseSettings.MAX_VALUES_PER_NETWORK.set(Integer.MAX_VALUE - 1);
		
        BucketRefresherSettings.UNIFORM_BUCKET_REFRESH_DISTRIBUTION.setValue(true);

		// See http://wiki.limewire.org/index.php?title=Debug_logging for more information
		// on using the LimeWire logging
		System.out.println("Storable Republish time " + DatabaseSettings.STORABLE_PUBLISHER_PERIOD.getValue() / 1000 + " seconds.");
		System.out.println("Value Expiration time " + DatabaseSettings.VALUE_EXPIRATION_TIME.getValue() / 1000 + " seconds.");
		System.out.println("Value republish time " + DatabaseSettings.VALUE_REPUBLISH_INTERVAL.getValue() / 1000 + " seconds.");
		System.out.println("Max values per address " + DatabaseSettings.MAX_VALUES_PER_ADDRESS.getValue() + ".");
		System.out.println("Max values per network " + DatabaseSettings.MAX_VALUES_PER_NETWORK.getValue() + ".");		
		System.out.println("Replication setting size: " + KademliaSettings.REPLICATION_PARAMETER.getValue());

	}
	
	/**
	 * Creates the DHT nodes from the localhost:port and bootstraps.
	 * @count the number of nodes 
	 * @port the starting port to use
	 */
    public void bootstrap(int count, int port) {
    			
    	dhtSettings();        
        
        dhts = standalone(null, port, count);
        
        if (dhts.isEmpty()) {
            System.out.println("No Nodes to run");
            System.exit(0);
        }
                
        long time = 0L;
        
        dhts.get(0).start();
            
        bootstrapHost = new InetSocketAddress("localhost", port);

        //0 is the central node
        for(int i = 1; i < dhts.size(); i++) {
            MojitoDHT dht = dhts.get(i);
            dht.start();
            
            BootstrapResult result = null;
			try {
				result = dht.bootstrap(bootstrapHost).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
            
            time += result.getTime();
            
            if (result.getResultType().equals(ResultType.BOOTSTRAP_SUCCEEDED)) {    
            	System.out.println("Node #" + i + " finished bootstrapping from " 
                        + bootstrapHost + " in " + result.getTime() + "ms");
            } else {
            	System.out.println("Node #" + i + " failed to bootstrap from " 
                        + bootstrapHost + "\n" + result);
            }
        }

        BootstrapResult result = null;
		try {
			result = MojitoUtils.bootstrap(
			        dhts.get(0), dhts.get(1).getContactAddress()).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        time += result.getTime();
        
        if (result.getResultType().equals(ResultType.BOOTSTRAP_SUCCEEDED)) {    
        	System.out.println("Node #0 finished bootstrapping from " 
                    + dhts.get(1).getContactAddress() + " in " + result.getTime() + "ms");
        } else {
        	System.out.println("Node #0 failed to bootstrap from " 
                    + dhts.get(1).getContactAddress() + "\n" + result);
        }
      	System.out.println("All Nodes finished bootstrapping in " + time + "ms");
	}
        
    private static List<MojitoDHT> standalone(InetAddress addr, int port, int count) {
        
        List<MojitoDHT> dhts = new ArrayList<MojitoDHT>(count);
        
        for(int i = 0; i < count; i++) {
            try {
                MojitoDHT dht = MojitoFactory.createDHT("Tutorial node port index:" + i);
                
                if (addr != null) {
                    dht.bind(new InetSocketAddress(addr, port+i));
                } else {
                    dht.bind(new InetSocketAddress(port+i));
                }
                
                dhts.add(dht);
                
               	System.out.println(i + ": " + ((Context)dhts.get(i)).getLocalNode());
            } catch (IOException err) {
                System.err.println("Failed to start/connect DHT #" + i);
                err.printStackTrace();
            }
        }
        
        return dhts;
    }

    /**
     * Converts a String into a <code>KUID</code> used to uniquely identify
     * a published value.
     * @param value in this tutorial this value is the title of a book
     */
    public static KUID createKey(String value) {						
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeErrorException(null, "Unable to create the KUID");
		}
		KUID key = null;
		try {
			key = KUID.createWithBytes(md.digest(value.getBytes("UTF-8")));
			md.reset();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		};
		return key;

    }

    /**
     * Publish custom values (BookReviewsDHTValue) into the Tutorial DHT.
     */
    public void blockingPutCustomValues() {
    	
    	int nodeIndex = 0;
		for(Book bookReview : bookReviews) {
			
	    	// Inserts values into the Tutorial DHT for only ports 11000-11019 
			if(nodeIndex >= 20) {
				nodeIndex = 0;
			}
			MojitoDHT dht = dhts.get(nodeIndex);

        	KUID keyTitle = createKey(bookReview.getTitle());
        	
  			try {
				dht.put(keyTitle, new BookReviewsDHTValue(bookReview)).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
    		nodeIndex++;
    	}
    }
    
	/**
	 * Searches the Tutorial DHT for reviews of a title.
	 */
	public void retrieve() {
		
		System.out.println("Search type: *" + BookReviewsDHTValue.BOOK.getName() + "* for " 
				+ "\n\tKUID: " + createKey("Adventures of Huckelberry Finn").toString()
				+ "\n\ttitle: " + "Adventures of Huckelberry Finn" 
				+ "\n\tfrom port index: " + 50);
		getValue(createKey("Adventures of Huckelberry Finn"), 50);
	}

	/**
	 * Gets a value from the tutorial DHT in a blocking way.
	 * @param title which book to find reviews
	 * @param nodeIndex allows searches from different nodes on the Tutorial DHT
	 */

	protected void getValue(KUID keyTitle, int nodeIndex) {
		System.out.println("Search for KUID: " + keyTitle.toHexString() 
			+ " on port index: " + nodeIndex);
		
	    final MojitoDHT dht = dhts.get(nodeIndex);
	    EntityKey lookupKey = EntityKey.createEntityKey(keyTitle, BookReviewsDHTValue.BOOK);	    	
	    	
	    FindValueResult result = null;
	    try {
			result = dht.get(lookupKey).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}			
		iterateForBook(result, dht);

    }
	
	//Logic:
	//If an entity, loop through the DHTValueEntity
	
	//If an entity key, loop through each EntityKey and do a get for each EntityKey 
	//using the primary key
    private void iterateForBook(FindValueResult findValueResult, final MojitoDHT dht) {

    	//The DHTValueEntity is a row in the database 
    	for(DHTValueEntity entity : findValueResult.getEntities()) {
			String bookValue = new String(entity.getValue().getValue());
			Book book = new Book(bookValue);
			System.out.println("DHTValueEntity - BookReviewResultListener, KUID: " + entity.getPrimaryKey().toHexString() 
					+ "\n\tsecondary KUID: " + entity.getSecondaryKey().toHexString() 		
					+ "\n\ttitle: " + book.getTitle() 
					+ "\n\trating: " + book.getRating() 
					+ "\n\tauthor: " + book.getAuthor() 
					);				
			
    	}

    	//An EntityKey specifies the exact location of a DHTValueEntity.
    	for(EntityKey entityKey : findValueResult.getEntityKeys()) {

			//full lookup key using contact, primary and secondary
			EntityKey retrieveWithSecondary = EntityKey.createEntityKey(entityKey.getContact(), entityKey.getPrimaryKey(), entityKey.getSecondaryKey(), entityKey.getDHTValueType());
			FindValueResult resultWithSecondary = null;
			try {
				resultWithSecondary = dht.get(retrieveWithSecondary).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			iterateForBook(resultWithSecondary, dht);
					
    	}
    }
}