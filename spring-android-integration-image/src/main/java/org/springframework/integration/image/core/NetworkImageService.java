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

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.disk.NoLimitDiskCache;
import org.springframework.cache.disk.core.FileTemplate;
import org.springframework.cache.disk.serializer.BitmapCacheValueSerializer;
import org.springframework.cache.disk.serializer.SimpleHashCacheKeySerializer;
import org.springframework.http.HttpMethod;
import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.image.ImageHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.client.RestTemplate;

import android.content.Intent;
import android.graphics.Bitmap;

public class NetworkImageService extends AbstractImageService {

    public NetworkImageService() {
        super("NetworkImageService");
    }

    public NetworkImageService(String name) {
        super(name);
    }
    
    @Override
    protected Cache initCache() {
        FileTemplate<String, Bitmap> template = new FileTemplate<String, Bitmap>();
        template.setCacheValueSerializer(new BitmapCacheValueSerializer());
        template.setCacheKeySerializer(new SimpleHashCacheKeySerializer());
        File dir = new File(getBaseContext().getCacheDir(), "NetworkImageServiceDiskCache");
        NoLimitDiskCache<String, Bitmap> cache = new NoLimitDiskCache<String, Bitmap>("NetworkImageServiceDiskCache", dir, template);
        return cache;
    }

    @Override
    protected Message<?> createMessage(Intent intent) {
        Map<String, String> urivars = new LinkedHashMap<String, String>();
        if(intent.hasExtra(HttpHeaders.REQUEST_URL)) {
            urivars.put(HttpHeaders.REQUEST_URL, intent.getStringExtra(HttpHeaders.REQUEST_URL));
        } else {
            return null;
        }
        
        MessageBuilder<HashMap> builder = MessageBuilder.withPayload(new HashMap())
            .setHeader(HttpHeaders.URI_VARIABLES, urivars);
        
        if(intent.hasExtra(ImageHeaders.TAGS)) {
            String[] stringArrayExtra = intent.getStringArrayExtra(ImageHeaders.TAGS);
            for(int i = 0; i<stringArrayExtra.length; i+=2) {
                builder.setHeader(stringArrayExtra[i], stringArrayExtra[i+1]);
            }
            
        }
        
        if(intent.hasExtra(ImageHeaders.NOCACHE)) {
            builder.setHeader(ImageHeaders.NOCACHE, "true");
        }
        
        Message<?> message = builder.build();
        
        return message;
    }
    
    @Override
    protected AbstractReplyProducingMessageHandler[] getMessageHandlers() {
        RestTemplate restTemplate = new RestTemplate(true);
        restTemplate.getMessageConverters().add(0, new BitmapHttpMessageConverter());
        String uri = "{" + HttpHeaders.REQUEST_URL + "}";
        HttpRequestExecutingMessageHandler messageHandler = new HttpRequestExecutingMessageHandler(uri, restTemplate);
        messageHandler.setHttpMethod(HttpMethod.GET);
        return new AbstractReplyProducingMessageHandler[]{messageHandler};
    }

}
