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
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.disk.serializer.CacheKeySerializer;
import org.springframework.cache.disk.serializer.CacheValueSerializer;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * Helper class that simplifies file access operations.
 * 
 * @author Janne Valkealahti
 *
 * @param <K> the type of keys used in this class
 * @param <V> the type of values used in this class
 */
public class FileTemplate<K, V> {

    /** Serializer for values to be stored in files */
    @SuppressWarnings("rawtypes")
    private CacheValueSerializer mValueSerializer = null;
    
    /** Serializer for keys to match file name */
    @SuppressWarnings("rawtypes")
    private CacheKeySerializer mKeySerializer = null;

    public FileTemplate() {
    }

    /**
     * Returns the cache value serializer used by this template.
     * 
     * @return the cache value serializer used by this template.
     */
    public CacheValueSerializer<?> getValueSerializer() {
        return mValueSerializer;
    }

    /**
     * Sets the cache value serializer to be used by this template. Defaults to {@link #getDefaultSerializer()}.
     * 
     * @param serializer the cache value serializer to be used by this template.
     */
    public void setCacheValueSerializer(CacheValueSerializer<?> serializer) {
        mValueSerializer = serializer;
    }

    /**
     * Returns the cache key serializer used by this template.
     * 
     * @return the cache key serializer used by this template.
     */
    public CacheKeySerializer<?> getKeySerializer() {
        return mKeySerializer;
    }

    /**
     * Sets the cache key serializer to be used by this template. Defaults to {@link #getDefaultSerializer()}.
     * 
     * @param serializer the cache key serializer to be used by this template.
     */
    public void setCacheKeySerializer(CacheKeySerializer<?> serializer) {
        mKeySerializer = serializer;
    }

    @SuppressWarnings("unchecked")
    public void write(K key, V value, File directory) {
        final File file = new File(directory, mKeySerializer.serialize(key));
        final byte[] bytes = mValueSerializer.serialize(value);
        execute(new FileCallback<Object>() {
            @Override
            public Object doWithFile(File file) throws IOException {
                FileCopyUtils.copy(bytes, file);
                return null;
            }
        }, file);        
    }

    @SuppressWarnings("unchecked")
    public String writeCheckNew(K key, V value, File directory) {
        final File file = new File(directory, mKeySerializer.serialize(key));
        final byte[] bytes = mValueSerializer.serialize(value);
        return execute(new FileCallback<String>() {
            @Override
            public String doWithFile(File file) throws IOException {
                boolean exists = file.exists();
                FileCopyUtils.copy(bytes, file);
                return exists ? null : file.getName();
            }
        }, file);        
    }
    
    @SuppressWarnings("unchecked")
    public V read(K key, File directory) {
        final File file = new File(directory, mKeySerializer.serialize(key));
        return (V) execute(new FileCallback<Object>() {
            @Override
            public Object doWithFile(File file) throws IOException {
                if(file.exists()) {
                    return mValueSerializer.deserialize(FileCopyUtils.copyToByteArray(file));                    
                } else {
                    return null;
                }
            }
        }, file);
    }
    
    @SuppressWarnings("unchecked")
    public void delete(K key, File directory) {
        final File file = new File(directory, mKeySerializer.serialize(key));
        execute(new FileCallback<Object>() {
            @Override
            public Object doWithFile(File file) {
                if(file.isFile()) {
                    file.delete();
                }
                return null;
            }
        }, file);        
    }
    
    public int countFilesInDirectory(File directory) {
        return execute(new FileCallback<Integer>() {
            @Override
            public Integer doWithFile(File file) {
                return file.list().length;
            }            
        }, directory);
    }

    public Map<String, Long> modifyTimes(File directory) {
        return execute(new FileCallback<Map<String, Long>>() {
            @Override
            public Map<String, Long> doWithFile(File file) {
                Map<String, Long> map = new HashMap<String, Long>();
                for(File f : file.listFiles()) {
                    map.put(f.getName(), f.lastModified());
                }
                return map;
            }            
        }, directory);
    }

    public String[] filesWithModifyOrder(File directory) {
        return execute(new FileCallback<String[]>() {
            @Override
            public String[] doWithFile(File file) {
                File[] files = file.listFiles();
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File lhs, File rhs) {
                        return Long.valueOf(lhs.lastModified()).compareTo(rhs.lastModified());
                    }
                });
                String[] sorted = new String[files.length];
                for(int i = 0; i<files.length; i++) {
                    sorted[i] = files[i].getName();
                }
                return sorted;
            }            
        }, directory);
    }
    
    /**
     * Simple pass-through to {@link File#isFile()} method.
     * 
     * @param key the cache key
     * @param directory the cache directory
     * @return True if resolved file from key is a file, false otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean isFile(K key, File directory) {
        final File file = new File(directory, mKeySerializer.serialize(key));
        return execute(new FileCallback<Boolean>() {
            @Override
            public Boolean doWithFile(File file) {
                return file.isFile();
            }            
        }, file);
    }

    /**
     * Execute the action specified by the given callback object within a {@link File}.
     * 
     * @param callback callback object that specifies the File action
     * @param file file handle to work with
     * @return a result object returned by the callback, or null
     * @throws DataAccessException in case of file errors
     */
    public <T> T execute(FileCallback<T> callback, File file) throws DataAccessException {
        Assert.notNull(callback, "Callback object must not be null");
        try {
            return callback.doWithFile(file);
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw new DataAccessResourceFailureException(e.getMessage(), e);
        }   
    }
    
}
