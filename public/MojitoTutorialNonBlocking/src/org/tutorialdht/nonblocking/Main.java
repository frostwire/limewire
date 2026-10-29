package org.tutorialdht.nonblocking;
import java.util.concurrent.ExecutionException;

import org.tutorialdht.core.BookReviewsDHT;

/**
 * Creates a 100 node Tutorial DHT and publishes book reviews in a blocking manner. 
 * Then this sample performs one search. 
 * <p>
 * All output is shown in the console.
 */

public class Main {
	public static void main(String[] args) throws ExecutionException, Exception {    	    	
    	
		BookReviewsDHT bookReviewsDHT = new BookReviewsDHT();
        //100 nodes with 11000 being the first node's port
        //2nd node = port 11001, 3rd node = 11002, ...
		bookReviewsDHT.bootstrap(100, 11000);		
		
		bookReviewsDHT.nonblockingStoreCustomValues();
		 			
		try {
			// Publishing is scheduled for every 10 seconds. You need to wait at least 10
			// seconds in this tutorial in order to receive search results.
			System.out.println("Sleep for 35 seconds in main to give time for publishing.");
			Thread.sleep(35 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bookReviewsDHT.retrieve();
				
		bookReviewsDHT.closeDHT();
    }
}
