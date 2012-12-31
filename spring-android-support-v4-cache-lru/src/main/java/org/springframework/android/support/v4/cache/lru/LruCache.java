/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.android.support.v4.cache.lru;

import java.io.Serializable;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

/**
 * Cache implementation which implements SpringFramework {@link Cache}
 * interface and wraps {@link android.support.v4.util.LruCache} from
 * Android's v4 support package.
 * <p>
 * There are similar implementation in {@link org.springramework.cache.lru.LruCache}
 * which wraps {@link android.util.LruCache} introduced in
 * api level 12.
 * 
 * @author Janne Valkealahti
 */
@SuppressWarnings("rawtypes")
public class LruCache implements Cache {

    private static final Object NULL_HOLDER = new NullHolder();
    private final String mName;
    private final android.support.v4.util.LruCache mLruCache;
    private final boolean mAllowNullValues;

    /**
     * Create a new LruCache with the specified name and
     * maximum size of the cache.
     * @param name the name of the cache
     * @param maxSize the max size of the cache
     */
    public LruCache(String name, int maxSize) {
        this(name, maxSize, true, new android.support.v4.util.LruCache(maxSize));
    }
    
    /**
     * Create a new LruCache with the specified name and
     * maximum size of the cache.
     * @param name the name of the cache
     * @param maxSize the max size of the cache
     * @param allowNullValues whether to accept and convert null values for this cache
     * @param cache the cache to use as internal store
     */
    public LruCache(String name, int maxSize, boolean allowNullValues, android.support.v4.util.LruCache cache) {
        mName = name;
        mAllowNullValues = allowNullValues;
        mLruCache = cache;
    }

    @Override
    public String getName() {
        return mName;
    }

    /**
     * Returns the underlying cache implementation
     * {@link android.support.v4.util.LruCache}.
     */
    @Override
    public Object getNativeCache() {
        return mLruCache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueWrapper get(Object key) {
        Object value = mLruCache.get(key);
        return (value != null ? new SimpleValueWrapper(fromStoreValue(value)) : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void put(Object key, Object value) {
        mLruCache.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void evict(Object key) {
        mLruCache.remove(key);
    }

    @Override
    public void clear() {
        mLruCache.evictAll();
    }

    /**
     * Convert the given value from the internal store to a user value
     * returned from the get method (adapting <code>null</code>).
     * @param storeValue the store value
     * @return the value to return to the user
     */
    protected Object fromStoreValue(Object storeValue) {
        if (this.mAllowNullValues && storeValue == NULL_HOLDER) {
            return null;
        }
        return storeValue;
    }

    /**
     * Convert the given user value, as passed into the put method,
     * to a value in the internal store (adapting <code>null</code>).
     * @param userValue the given user value
     * @return the value to store
     */
    protected Object toStoreValue(Object userValue) {
        if (this.mAllowNullValues && userValue == null) {
            return NULL_HOLDER;
        }
        return userValue;
    }
    
    @SuppressWarnings("serial")
    private static class NullHolder implements Serializable {
    }
    
}
