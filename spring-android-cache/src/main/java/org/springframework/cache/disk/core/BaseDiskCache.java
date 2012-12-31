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

package org.springframework.cache.disk.core;

import java.io.File;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

/**
 * Base cache implementation which knows the directory it works
 * against and {@link FileTemplate} used for file operations.
 * 
 * @author Janne Valkealahti
 *
 * @param <K> the type of keys used in this cache
 * @param <V> the type of values used in this cache
 */
@SuppressWarnings("unchecked")
public abstract class BaseDiskCache<K, V> implements Cache {
    
    /** Name of this cache */
    private String mName;
    /** Cache directory */
    private File mDir;
    /** Template used by this cache */
    @SuppressWarnings("rawtypes")
    private final FileTemplate mTemplate;

    public BaseDiskCache(String name, File dir, FileTemplate<? extends Object, ? extends Object> template) {
        mName = name;
        mDir = dir;
        mTemplate = template;
        dir.mkdirs();
    }
    
    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Object getNativeCache() {
        return mDir;
    }

    @Override
    public ValueWrapper get(Object key) {        
        Object object = mTemplate.read(key, mDir);
        return object == null ? null : new SimpleValueWrapper(object);
    }
    
    @Override
    public void put(Object key, Object value) {        
        mTemplate.write(key, value, mDir);
    }

    @Override
    public void evict(Object key) {
        File f = new File(mDir, key.toString());
        if(f.exists()) {
            f.delete();
        }        
    }

    @Override
    public void clear() {
        for(File f : mDir.listFiles()) {
            f.delete();            
        }
    }
    
    /**
     * Gets a directory used by this cache.
     * @return directory for this cache
     */
    protected File getDirectory() {
        return mDir;
    }
    
    /**
     * Gets the {@link FileTemplate} used by this cache.
     * @return the file template used by this cache
     */
    @SuppressWarnings("rawtypes")
    protected FileTemplate getTemplate() {
        return mTemplate;
    }
    
}
