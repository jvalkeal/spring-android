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

package org.springframework.cache.disk;

import java.io.File;
import java.io.IOException;

import org.springframework.cache.disk.core.BaseDiskCache;
import org.springframework.cache.disk.core.FileCallback;
import org.springframework.cache.disk.core.FileTemplate;

import android.annotation.TargetApi;
import android.util.LruCache;


/**
 * Cache implementation which provided functionality to limit a number
 * of files which exist in a cache directory. Eviction of files is based
 * on least recently used access of cached files.
 * <p>
 * This class has dependency to api level 12. If you need to target
 * application for lower api levels, use similar implementation 
 * {@link org.springframework.android.support.v4.cache.disk.FileCountLimitDiskCache}
 * which is depends on v4 support package.
 * 
 * @author Janne Valkealahti
 *
 * @param <K> the type of keys used in this cache
 * @param <V> the type of values used in this cache
 */
public class FileCountLimitDiskCache<K, V> extends BaseDiskCache<K, V> {

    /** Map to store access times */
    private EvictionLruCache mDiskElements;
    /** Simple object because we don't need values in lru map*/
    private static final Object NULL_HOLDER = new Object();
    
    @TargetApi(12)
    public FileCountLimitDiskCache(String name, File dir, FileTemplate<? extends Object, ? extends Object> template, int limit) {
        super(name, dir, template);
        mDiskElements = new EvictionLruCache(limit);
        initStats();
    }

    @Override
    public ValueWrapper get(Object key) {
        String cacheKey = getTemplate().getKeySerializer().serialize(key);
        mDiskElements.get(cacheKey);
        return super.get(key);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void put(Object key, Object value) {
        String newFileName = getTemplate().writeCheckNew(key, value, getDirectory());
        if(newFileName != null) {
            mDiskElements.put(newFileName, System.currentTimeMillis());
        }
    }
    
    protected void initStats() {
        String[] files = getTemplate().filesWithModifyOrder(getDirectory());
        for(String file : files) {
            mDiskElements.put(file, NULL_HOLDER);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void removeFile(String name) {
        File file = new File(getDirectory(), name);
        getTemplate().execute(new FileCallback<Object>() {
            @Override
            public Object doWithFile(File file) throws IOException {
                file.delete();
                return null;
            }
        }, file);
    }
    
    @TargetApi(12)
    private class EvictionLruCache extends LruCache<String, Object> {

        public EvictionLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, Object oldValue, Object newValue) {
            // lru cache notifies us that entry needs to be removed
            // so fire operation to remove file 
            removeFile(key);
        }
        
    }
    
}
