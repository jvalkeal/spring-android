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

import org.springframework.cache.Cache;
import org.springframework.cache.disk.core.FileTemplate;
import org.springframework.cache.disk.serializer.SimpleCacheKeySerializer;
import org.springframework.cache.disk.serializer.StringCacheValueSerializer;

import android.test.AndroidTestCase;

/**
 * Tests for NoLimitDiskCache.
 * 
 * @author Janne Valkealahti
 */
public class NoLimitDiskCacheTests extends AndroidTestCase {

    final static String KEY = "file";
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

    public void testCacheOperations() {
        mCache = new NoLimitDiskCache<String, String>("testDiskCache", mDir, mTemplate);
        for(int i = 0; i<100; i++) {
            mCache.put(KEY+i, VALUE+i);
        }
        assertEquals(100, mTemplate.countFilesInDirectory(mDir));
        for(int i = 0; i<100; i++) {
            assertEquals(VALUE+i, (String)mCache.get(KEY+i).get());
        }
    }

    private File getCacheDir() {
        return new File(getContext().getCacheDir(), "test");
    }

}
