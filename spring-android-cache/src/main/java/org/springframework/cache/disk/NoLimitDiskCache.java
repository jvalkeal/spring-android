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

import org.springframework.cache.disk.core.BaseDiskCache;
import org.springframework.cache.disk.core.FileTemplate;

/**
 * Simple disk cache implementation which doesn't force any kind
 * of eviction operations nor limitation of cache size.
 * <p>
 * NOTE: It's up for user to evict old entries.
 * 
 * @author Janne Valkealahti
 * 
 * @param <K> the type of keys used in this cache
 * @param <V> the type of values used in this cache
 */
public class NoLimitDiskCache<K, V> extends BaseDiskCache<K, V> {

    public NoLimitDiskCache(String name, File dir, FileTemplate<? extends Object, ? extends Object> template) {
        super(name, dir, template);
    }

}
