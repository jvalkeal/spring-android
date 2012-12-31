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

package org.springframework.integration.image.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.integration.Message;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.image.support.ImageOptions;
import org.springframework.integration.image.support.SimpleBitmapWrapper;
import org.springframework.integration.image.support.ImageOptions.ImageSource;
import org.springframework.util.Assert;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Base implementation of bitmap manager which is aware of a set of memory
 * caches where bitmaps may exist. If requested bitmap is found from a
 * registered caches it is returned immediately without further processing,
 * however if cache operation result a cache miss, further processing will
 * happen in a image processing service which eventually will return bitmap
 * through a series of callbacks. Processed bitmap will be cached with the
 * policy defined in underlying caches.
 * 
 * @author Janne Valkealahti
 */
public abstract class CachingBitmapManager extends AbstractBitmapManager {

    private Cache mCache;

    public CachingBitmapManager(Context context) {
        this(context, NetworkImageService.class);
    }
    
    public CachingBitmapManager(Context context, Class<?> serviceClazz) {
        super(context, serviceClazz);
        mCache = initCache();
        Assert.notNull(mCache, "Cache can't be null");
    }

    protected abstract Cache initCache();

    @Override
    public BitmapWrapper requestBitmap(ImageOptions options) {
        
        ValueWrapper valueWrapper = mCache.get(options.getUrl());
        if(valueWrapper != null) {
            return new SimpleBitmapWrapper((Bitmap)valueWrapper.get());
        }
        
        if(options.getImageSource() == ImageSource.HTTP) {
            requestNetworkImage(options);
        }
        return new SimpleBitmapWrapper(null);
    }
    
    @Override
    protected void onInternalReceiveResult(Message<?> message) {
        Map<String,String> map = (Map<String, String>) message.getHeaders().get(HttpHeaders.URI_VARIABLES);
        String key = null;
        if(map != null) {
            key = map.get(HttpHeaders.REQUEST_URL);
        }
        Bitmap bitmap = (Bitmap)message.getPayload();
        mCache.put(key, bitmap);
        
        Map<String, String> map2 = new HashMap<String, String>();
        for(Entry<String, Object> entry : message.getHeaders().entrySet()) {
            if(entry.getValue() instanceof String) {
                map2.put(entry.getKey(), (String)entry.getValue());
            }
        }        
        notifyObserver(bitmap, map2);
        
    }

}
