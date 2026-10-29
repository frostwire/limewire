/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.pool;

/**
 * A simple base impementation of {@link ObjectPool}.
 * All optional operations are implemented as throwing
 * {@link UnsupportedOperationException}.
 *
 * @author Rodney Waldhoff
 * @version $Revision: 1.3 $ $Date: 2006/07/13 18:36:40 $
 */
public abstract class BaseObjectPool<V> implements ObjectPool<V> {
    public abstract V borrowObject();
    public abstract void returnObject(V obj);
    public abstract void invalidateObject(V obj);

    /**
     * Not supported in this base implementation.
     */
    public int getNumIdle() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported in this base implementation.
     */
    public int getNumActive() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported in this base implementation.
     */
    public void clear() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported in this base implementation.
     */
    public void addObject() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public void close() {
        assertOpen();
        closed = true;
    }

    /**
     * Not supported in this base implementation.
     */
    public void setFactory(PoolableObjectFactory<V> factory) throws IllegalStateException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    protected final boolean isClosed() {
        return closed;
    }
    
    protected final void assertOpen() throws IllegalStateException {
        if(isClosed()) {
            throw new IllegalStateException("Pool not open");
        }
    }
    
    private volatile boolean closed = false;
}
