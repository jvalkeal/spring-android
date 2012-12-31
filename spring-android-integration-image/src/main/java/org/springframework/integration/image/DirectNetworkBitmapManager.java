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

package org.springframework.integration.image;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.integration.Message;
import org.springframework.integration.image.core.AbstractBitmapManager;
import org.springframework.integration.image.core.NetworkImageService;
import org.springframework.integration.image.support.ImageOptions;
import org.springframework.integration.image.support.SimpleBitmapWrapper;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Bitmap manager which always requests images which exists behind io
 * operation(either network or local storage) and doesn't do any caching. This
 * manager always return 'void' images and dispatches io requests to receive
 * images from "remote" sources and then executes callback for parties
 * registered for interest.
 * 
 * @author Janne Valkealahti
 */
public class DirectNetworkBitmapManager extends AbstractBitmapManager {

    public DirectNetworkBitmapManager(Context context) {
        this(context, NetworkImageService.class);
    }

    public DirectNetworkBitmapManager(Context context, Class<?> serviceClazz) {
        super(context, serviceClazz);
    }
    
    @Override
    public BitmapWrapper requestBitmap(ImageOptions options) {
        requestNetworkImage(options);
        return new SimpleBitmapWrapper(null);
    }
    
    @Override
    protected void onInternalReceiveResult(Message<?> message) {
        Bitmap bitmap = (Bitmap)message.getPayload();
        
        Map<String, String> map = new HashMap<String, String>();
        for(Entry<String, Object> entry : message.getHeaders().entrySet()) {
            if(entry.getValue() instanceof String) {
                map.put(entry.getKey(), (String)entry.getValue());
            }
        }        
        notifyObserver(bitmap, map);
    }

}
