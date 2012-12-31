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

package org.springframework.cache.disk.serializer;

import android.test.AndroidTestCase;

/**
 * Tests for SimpleHashCacheKeySerializer.
 * @author Janne Valkealahti
 */
public class SimpleHashCacheKeySerializerTests extends AndroidTestCase {

    public void testSimpleConcurrentAccess() throws InterruptedException {
        
        // this is a very naive test to make sure that our serializer
        // can handle concurrent access. early implementation made it
        // very easy to crash the dalvik because MessageDigest is not
        // exactly thread safe and according to
        // http://code.google.com/p/android/issues/detail?id=8709
        // it's 'normal' that there is a crash.
        
        final SimpleHashCacheKeySerializer serializer = new SimpleHashCacheKeySerializer();
        
        Runnable runnable = new Runnable() {
            @Override public void run() {
                for(int i = 0; i<10000; i++) {
                    serializer.serialize("foo");
                }
            }           
        };
        
        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        Thread t3 = new Thread(runnable);
        Thread t4 = new Thread(runnable);
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        
        // naive test, we just expect test to go through and not crash
    }

}
