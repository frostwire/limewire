package org.tutorialdht.core;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.limewire.mojito.db.Storable;
import org.limewire.mojito.db.StorableModel;
import org.limewire.mojito.result.StoreResult;

/**
 * Used to publish values. Mojito has an internal publishing task and queue. 
 * Every now and then, Mojito goes through all registered <code>StorableModel</code>s 
 * and enqueues the returned <code>Storable</code> elements in the publisher queue.
 */
public class BookReviewsStorableModel implements StorableModel {

    private List<Storable> storables = new LinkedList<Storable>();
    
    public BookReviewsStorableModel(){
    	storables.clear();
    }
    public void put(Storable storable) {    	
    	storables.add(storable);
    }

    @Override
    public Collection<Storable> getStorables() {
        return storables;
    }
    /**
     * You need to decide whether to republish or not.
     */
    @Override
    public void handleStoreResult(Storable value, StoreResult result) {
    }

    @Override
    public void handleContactChange() {
    }
}
