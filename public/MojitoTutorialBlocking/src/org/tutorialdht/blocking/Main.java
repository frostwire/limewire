package org.tutorialdht.blocking;
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
		bookReviewsDHT.bootstrap(100, 11000);
		
		bookReviewsDHT.blockingPutCustomValues();
							
		bookReviewsDHT.retrieve();
		    	    	    	
		bookReviewsDHT.closeDHT();
    }
}
