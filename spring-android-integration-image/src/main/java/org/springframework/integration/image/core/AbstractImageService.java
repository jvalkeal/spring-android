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

import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.AbstractSubscribableChannel;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.integration.gateway.AbstractAsyncGatewayService;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.image.ImageHeaders;
import org.springframework.integration.support.MessageBuilder;

import android.graphics.Bitmap;

/**
 * Base image service implementations where image request may come from
 * a local cache or from a channel handlers registered in sub classes.
 * Default implementation of this class is not using a local caching.
 * 
 * @author Janne Valkealahti
 */
public abstract class AbstractImageService extends AbstractAsyncGatewayService  {

    private Cache mCache;
    
    public AbstractImageService(String name) {
        super(name);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mCache = initCache();
    }
    
    /**
     * Initialise a local cache. Default implementation sets cache to
     * null which effectively disables caching. Although cache can be any
     * implementing {@link Cache} abstraction, usually inside this service
     * class it should be something which helps with IO operations, like
     * a disk cache.
     * @return Cache to be used.
     */
    protected Cache initCache() {
        return null;
    }
    
    // TODO: should check if interceptors should be added or not, but for
    //       now problem is that post process methods are called before
    //       cache is initialised. maybe init cache before
    //       super.onCreate() is called, but it'd be nice to always
    //       call super method before other init onCreate code
    
    @Override
    protected void onOutboundChannelPostProcess(AbstractSubscribableChannel channel) {
        channel.addInterceptor(new CacheCheckInterceptor());
    }
    
    @Override
    protected void onInboundChannelPostProcess(AbstractSubscribableChannel channel) {
        channel.addInterceptor(new CacheAddInterceptor());
    }

    /**
     * Channel interceptor which adds inbound incoming bitmap to a cache. 
     */
    private class CacheAddInterceptor extends ChannelInterceptorAdapter {
        @SuppressWarnings("unchecked")
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if(mCache != null && !message.getHeaders().containsKey(ImageHeaders.NOCACHE)) {
                Map<String,String> map = (Map<String, String>) message.getHeaders().get(HttpHeaders.URI_VARIABLES);
                String key = null;
                if(map != null) {
                    key = map.get(HttpHeaders.REQUEST_URL);
                }            
                mCache.put(key, (Bitmap)message.getPayload());                            
            }
            return super.preSend(message, channel);                
        }
    }
    
    /**
     * Channel interceptor which does a cache check before message is
     * send to a channel. In case we have a cache hit we simply prevent
     * message flow to continue to outbound channel and create a new message
     * which will be handled by inbound adapter.
     */
    private class CacheCheckInterceptor extends ChannelInterceptorAdapter {
        @SuppressWarnings("unchecked")
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {

            if(mCache != null && !message.getHeaders().containsKey(ImageHeaders.NOCACHE)) {
                Map<String,String> map = (Map<String, String>) message.getHeaders().get(HttpHeaders.URI_VARIABLES);
                String key = null;
                if(map != null) {
                    key = map.get(HttpHeaders.REQUEST_URL);
                }
                
                ValueWrapper valueWrapper = mCache.get(key);
                if(valueWrapper != null) {
                    Message<?> msg = MessageBuilder
                            .withPayload((Bitmap)valueWrapper.get())
                            .copyHeaders(message.getHeaders())
                            .build();
                        sendToInbound(msg);
                    return null;
                }        
            }
            return super.preSend(message, channel);            
        }
    }
    
}
