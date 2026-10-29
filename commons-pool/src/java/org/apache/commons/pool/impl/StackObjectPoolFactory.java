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

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.ObjectPoolFactory;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * A factory for creating {@link StackObjectPool} instances.
 *
 * @see StackObjectPool
 * @see StackKeyedObjectPoolFactory
 *
 * @author Rodney Waldhoff
 * @version $Revision: 1.2 $ $Date: 2006/07/13 16:48:58 $ 
 */
public class StackObjectPoolFactory<V> implements ObjectPoolFactory<V> {
    public StackObjectPoolFactory() {
        this(null,StackObjectPool.DEFAULT_MAX_SLEEPING,StackObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY);
    }

    public StackObjectPoolFactory(int max) {
        this(null,max,StackObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY);
    }

    public StackObjectPoolFactory(int max, int init) {
        this(null,max,init);
    }

    public StackObjectPoolFactory(PoolableObjectFactory<V> factory) {
        this(factory,StackObjectPool.DEFAULT_MAX_SLEEPING,StackObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY);
    }

    public StackObjectPoolFactory(PoolableObjectFactory<V> factory, int max) {
        this(factory,max,StackObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY);
    }

    public StackObjectPoolFactory(PoolableObjectFactory<V> factory, int max, int init) {
        _factory = factory;
        _maxSleeping = max;
        _initCapacity = init;
    }

    public ObjectPool<V> createPool() {
        return new StackObjectPool<V>(_factory,_maxSleeping,_initCapacity);
    }

    protected PoolableObjectFactory<V> _factory = null;
    protected int _maxSleeping = StackObjectPool.DEFAULT_MAX_SLEEPING;
    protected int _initCapacity = StackObjectPool.DEFAULT_INIT_SLEEPING_CAPACITY;

}
