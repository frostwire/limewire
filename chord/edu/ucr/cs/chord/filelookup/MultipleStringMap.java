package edu.ucr.cs.chord.filelookup;


import com.sun.java.util.collections.*;
import com.limegroup.gnutella.*; 

/**
   TODO: Timeouts for mappings, for garbage collection purposes. <br>
   TODO: Change from current hard limit on values to FIFO model or equivalent. <p>

   In fact, changing to a FIFO might handle the garbage collection problem satisfactorily too. <br>
   However, a FIFO is susceptible to DOS attacks. What to do, what to do!? =)
*/

class MultipleStringMap implements Cloneable
{
      public static int VALUES_PER_KEY_LIMIT=100;

      // basically a character count.
      int byteSize=0;

      TreeMap store;
      public MultipleStringMap()
      {
	 store=new TreeMap(new Comparator() { public int compare(Object a, Object b) { return ((String)a).compareTo((String)b);}});
      }
      
      public void add(String key, String value)
      {
	 Object stored=store.get(key);
	 if(stored==null) {
	    byteSize+=key.length()+value.length();
	    store.put(key, value); 
	 }
	 else if(stored instanceof String && !stored.equals(value)) {
	    byteSize+=value.length();

	    HashSet valueSet=new HashSet();
	    valueSet.add(stored);
	    valueSet.add(value);
	    store.put(key,valueSet);
	 }
	 // WARNING: Hard limit on entries.
	 else if(stored instanceof Collection && !((Collection)stored).contains(value) && ((Collection)stored).size()<VALUES_PER_KEY_LIMIT) {
	    byteSize+=value.length();
	    ((Collection)stored).add(value);
	 }	       
      }
      
      public Object clone() {
	 try{
	    return super.clone();
	 }catch(CloneNotSupportedException e) {
	    e.printStackTrace();
	    return null;
	 }
      }

      /** Returns the number of characters in this map, keys and values counted. */
      int getByteSize() {
	 return byteSize;
      }

      public String getOne(String key)
      {
	 Object value=store.get(key);
	 
	 if(value instanceof String) 
	    return (String)value;
	 else if(value instanceof Collection)
	    return (String)((Collection)value).iterator().next();
	 else
	    return null;
      }

      public String[] getAll(String key)
      {
	 Object stored=store.get(key);

	 if(stored==null) 
	    return null;
	 else if(stored instanceof Collection)
	    return (String[])((Collection)stored).toArray(new String[((Collection)stored).size()]);
	 else if(stored instanceof String)
	    return new String[] {(String)stored};

	 return null;
      }

      public void addAll(String key, String[] values)
      {
	 for(int i=0;i<values.length;i++)
	    add(key,values[i]);
      }

      public Collection keySet()
      {
	 return store.keySet();
      }

      public int size() 
      {
	 return store.size();
      }

      /**
	 WARNING, TODO: This only compares keys. For some reason, the Collection.equals() methods don't work properly.
      */
      public boolean equals(MultipleStringMap other) 
      {	 
	 //System.out.println(store+"\n"+other.store);

	 // WARNING, BUG! Uncomment to remove bug, but then the Java bug will mess it up instead!
	 // return store.equals(other.store);
	 return store.keySet().equals(other.store.keySet());
      }

      public String toString() 
      {
	 return store.toString();
      }

      /** Adds all the entries in @param map to the store */
      public void putAll(MultipleStringMap map)
      {
	 this.store.putAll(map.store);
      }
}
