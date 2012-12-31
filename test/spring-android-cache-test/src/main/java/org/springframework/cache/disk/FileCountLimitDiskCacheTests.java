/*
 * Copyright 2012 the original author or authors.
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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.disk.core.FileTemplate;
import org.springframework.cache.disk.serializer.SimpleCacheKeySerializer;
import org.springframework.cache.disk.serializer.StringCacheValueSerializer;
import org.springframework.util.FileCopyUtils;

import android.os.SystemClock;
import android.test.AndroidTestCase;

/**
 * Tests for FileCountLimitDiskCache.
 * 
 * @author Janne Valkealahti
 */
public class FileCountLimitDiskCacheTests extends AndroidTestCase {

    final static String KEY = "file";
    final static String KEY1 = "file1";
    final static String KEY2 = "file2";
    final static String VALUE = "content";
    File mDir;
    Cache mCache;
    FileTemplate<String, String> mTemplate;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDir = getCacheDir();
        mDir.mkdir();
        assertNotNull(mDir);
        TestUtils.removeAllFiles(mDir);
        mTemplate = new FileTemplate<String, String>();
        mTemplate.setCacheValueSerializer(new StringCacheValueSerializer());
        mTemplate.setCacheKeySerializer(new SimpleCacheKeySerializer());
    }

    public void testStringWriteAndRead() throws FileNotFoundException, IOException {
        mCache = new FileCountLimitDiskCache<String, String>("testDiskCache", mDir, mTemplate, 5);
        mCache.put(KEY1, VALUE);
        assertEquals(VALUE, (String)mCache.get(KEY1).get());
    }
    
    public void testLimitStartEmptyDir() {
        mCache = new FileCountLimitDiskCache<String, String>("testDiskCache", mDir, mTemplate, 5);
        for(int i = 0; i<10; i++) {
            mCache.put(KEY+i, VALUE);
        }
        assertEquals(5, mTemplate.countFilesInDirectory(mDir));
    }

    public void testLimitStartWithFilesLessThanLimit() throws IOException {
        createDummyFile("dummy1");
        mCache = new FileCountLimitDiskCache<String, String>("testDiskCache", mDir, mTemplate, 5);
        for(int i = 0; i<10; i++) {
            mCache.put(KEY+i, VALUE+i);
        }
        assertEquals(5, mTemplate.countFilesInDirectory(mDir));
        // check that we have files file(5-9) matching content(5-9)
        for(int i = 5; i<10; i++) {
            assertEquals(VALUE+i, (String)mCache.get(KEY+i).get());
        }
    }

    public void testLimitStartWithFilesMoreThanLimit() throws IOException {
        for(int i = 0; i<10; i++) {
            createDummyFile("dummy"+i);
            // sleep so we get different modify times
            SystemClock.sleep(1000);
        }
        mCache = new FileCountLimitDiskCache<String, String>("testDiskCache", mDir, mTemplate, 5);
        SystemClock.sleep(1000);
        for(int i = 0; i<10; i++) {
            mCache.put(KEY+i, VALUE+i);
            // sleep so we get different modify times
            SystemClock.sleep(1000);
        }
        assertEquals(5, mTemplate.countFilesInDirectory(mDir));
        // check that we have files file(5-9) matching content(5-9)
        for(int i = 5; i<10; i++) {
            assertEquals(VALUE+i, (String)mCache.get(KEY+i).get());
        }
    }

    public void testLimitReadAndWrite() {
        mCache = new FileCountLimitDiskCache<String, String>("testDiskCache", mDir, mTemplate, 5);
        // insert first 5 values (0,1,2,3,4)
        for(int i = 0; i<5; i++) {
            mCache.put(KEY+i, VALUE+i);
            SystemClock.sleep(100);
        }
        assertEquals(5, mTemplate.countFilesInDirectory(mDir));

        // access first 3 values few times (0,1,2)
        for(int i = 0; i<3; i++) {
            mCache.get(KEY+0);
            SystemClock.sleep(100);
            mCache.get(KEY+1);
            SystemClock.sleep(100);
            mCache.get(KEY+2);
            SystemClock.sleep(100);
        }

        // insert new values (5,6,7)
        for(int i = 5; i<8; i++) {
            mCache.put(KEY+i, VALUE+i);
            SystemClock.sleep(100);
        }
        
        // we should have values 5,6,7 and 1,2
        ValueWrapper wrapper = mCache.get(KEY+1);
        assertNotNull(wrapper);
        assertEquals(VALUE+1, (String)wrapper.get());
        
        wrapper = mCache.get(KEY+2);
        assertNotNull(wrapper);
        assertEquals(VALUE+2, (String)wrapper.get());
        
        wrapper = mCache.get(KEY+5);
        assertNotNull(wrapper);
        assertEquals(VALUE+5, (String)wrapper.get());
                
        wrapper = mCache.get(KEY+6);
        assertNotNull(wrapper);
        assertEquals(VALUE+6, (String)wrapper.get());
        
        wrapper = mCache.get(KEY+7);
        assertNotNull(wrapper);
        assertEquals(VALUE+7, (String)wrapper.get());
    }
    
    private void createDummyFile(String name) throws IOException {
        FileCopyUtils.copy(name.getBytes(), new File(mDir, name));
    }

    private File getCacheDir() {
        return new File(getContext().getCacheDir(), "test");
    }

}
