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

package org.apache.commons.pool.impl;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;

/**
 * A factory for creating {@link StackKeyedObjectPool} instances.
 *
 * @see StackKeyedObjectPool
 * @see KeyedObjectPoolFactory
 *
 * @author Rodney Waldhoff
 * @version $Revision: 1.2 $ $Date: 2006/07/13 16:48:58 $ 
 */
public class StackKeyedObjectPoolFactory<V> implements KeyedObjectPoolFactory<V> {
    public StackKeyedObjectPoolFactory() {
        this(null,StackKeyedObjectPool.DEFAULT_MAX_SLEEPING,StackKeyedObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY);
    }

    public StackKeyedObjectPoolFactory(int max) {
        this(null,max,StackKeyedObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY);
    }

    public StackKeyedObjectPoolFactory(int max, int init) {
        this(null,max,init);
    }

    public StackKeyedObjectPoolFactory(KeyedPoolableObjectFactory<V> factory) {
        this(factory,StackKeyedObjectPool.DEFAULT_MAX_SLEEPING,StackKeyedObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY);
    }

    public StackKeyedObjectPoolFactory(KeyedPoolableObjectFactory<V> factory, int max) {
        this(factory,max,StackKeyedObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY);
    }

    public StackKeyedObjectPoolFactory(KeyedPoolableObjectFactory<V> factory, int max, int init) {
        _factory = factory;
        _maxSleeping = max;
        _initCapacity = init;
    }

    public KeyedObjectPool<V> createPool() {
        return new StackKeyedObjectPool<V>(_factory,_maxSleeping,_initCapacity);
    }

    protected KeyedPoolableObjectFactory<V> _factory = null;
    protected int _maxSleeping = StackKeyedObjectPool.DEFAULT_MAX_SLEEPING;
    protected int _initCapacity = StackKeyedObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY;

}
