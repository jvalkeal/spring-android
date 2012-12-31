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
import java.util.Map.Entry;

import org.springframework.integration.Message;
import org.springframework.integration.gateway.BaseServiceHelper;
import org.springframework.integration.gateway.MessageReceiver;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.image.BitmapManager;
import org.springframework.integration.image.BitmapObservable;
import org.springframework.integration.image.BitmapObserver;
import org.springframework.integration.image.ImageHeaders;
import org.springframework.integration.image.support.ImageOptions;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

/**
 * Base functionality to manage bitmap native operations, caching of bitmaps or
 * external requests to either process bitmaps from disk cache or other external
 * resource like fetching images over the network.
 * <p>
 * It is important to handle lifecycle methods of this manager. Default
 * implementation will call destroy methods when last observer goes away.
 * Failure to properly destroy this manager instance may keep binded
 * service alive and thus keep all its resources alive. This may for example,
 * keep all threads and thread pools active if service implementation
 * choose to use any of that stuff. Effectively this may cause a lot of leaked
 * threads and resources which may eventually result a dalvik crash.
 * 
 * @author Janne Valkealahti
 */
public abstract class AbstractBitmapManager implements BitmapManager {
    
    private Context mContext;
    private BaseServiceHelper mServiceHelper;
    private final BitmapObservable mObservable = new BitmapObservable();
    private Class<?> mServiceClazz;
    private boolean mAutoDestroy;

    public AbstractBitmapManager(Context context, Class<?> serviceClazz) {
        mContext = context;
        mServiceClazz = serviceClazz;
        mServiceHelper = new BaseServiceHelper(mContext, serviceClazz);
        mAutoDestroy = true;
        onCreate();
    }
    
    @Override
    public void registerBitmapObserver(BitmapObserver observer) {        
        // if we're a first one, make sure we have registered
        // our message receiver. need to do this in case manager instance
        // stayed alive but receiver were unregistered when last observer
        // went away.
        if(!mObservable.hasObservers()) {
            // if instance is same, service helper will not add it again
            mServiceHelper.addMessageReceiver(mMessageReceiver);
        }
        mObservable.registerObserver(observer);
    }

    @Override
    public void unregisterBitmapObserver(BitmapObserver observer) {
        mObservable.unregisterObserver(observer);
        // if manager lifecycle is set to auto destroy,
        // fire onDestroy if this was the last observer.
        if(mAutoDestroy && !mObservable.hasObservers()) {
            onDestroy();
        }
    }
    
    /**
     * Lifecycle method when this bitmap manager is started.
     */
    public void onCreate() {
        mServiceHelper.addMessageReceiver(mMessageReceiver);
    }

    /**
     * Lifecycle method when this bitmap manager is stopped.
     */
    public void onDestroy() {
        // unregister message listener which may tell binded
        // service to go away if service helper were a last binder.
        mServiceHelper.removeMessageReceiver(mMessageReceiver);
    }
    
    /**
     * Sets flag whether this manager should automatically call onDestroy
     * lifecycle method when last observer goes away.
     * @param autoDestroy the flag to set
     */
    public void setAutoDestroy(boolean autoDestroy) {
        mAutoDestroy = autoDestroy;
    }
    
    protected void requestNetworkImage(ImageOptions options) {
        Intent i = new Intent(mContext, mServiceClazz);
        i.putExtra(HttpHeaders.REQUEST_URL, options.getUrl());
        
        Map<String, String> tags = options.getTags();
        if(tags != null) {
            String[] array = new String[tags.size()*2];
            int n = 0;
            for(Entry<String, String> entry : tags.entrySet()) {
                array[n++] = entry.getKey();
                array[n++] = entry.getValue();
            }
            i.putExtra(ImageHeaders.TAGS, array);
        }
        
        mServiceHelper.handleIntent(i);        
    }
    
    protected void notifyObserver(Bitmap bitmap, Map<String, String> tags) {
        mObservable.notifyBitmap(bitmap, tags);
    }
    
    protected void onInternalReceiveResult(Message<?> message) {
        // space for rent
    }
    
    private MessageReceiver mMessageReceiver = new MessageReceiver() {
        @Override
        protected void onReceiveResult(Message<?> message) {
            onInternalReceiveResult(message);
        }
    };

}
