package edu.ucr.cs.chord.filelookup;

import edu.ucr.cs.chord.*;
import java.util.*;
import java.io.*;
import com.limegroup.gnutella.*;

/** 
    Interface for the Distributed File Lookup Table. 

    @author Jakob Eriksson
*/

public interface FileLookup
{
      /** 
	  Returns some or all of the uris mapped to the given urn. 
      */
      public String[] get(String urn) throws IOException;
      
      /** Adds a urn-uri mapping to the lookup table.
	  <p>TODO: Need an add() method with expiry date.
      */ 
      public void add(String urn, String uri) throws IOException;
}
