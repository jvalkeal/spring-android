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

package org.springframework.integration.gateway;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.integration.LogConstants;
import org.springframework.integration.Message;
import org.springframework.integration.gateway.AbstractGatewayService.GatewayServiceBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Base implementation of service helper pattern which makes
 * it much easier to work with service binding.
 * 
 * @author Janne Valkealahti
 */
public class BaseServiceHelper {
    
    private final static String TAG = "BaseServiceHelper";

    private Context mContext;
    private Queue<IntentHolder> mRequestQueue;
    private ArrayList<MessageReceiver> mReceivers;
    private boolean mServiceBound = false;
    private AtomicInteger mRequestId = new AtomicInteger();
    private AbstractRequestGatewayService mService;
    private Class<?> mServiceClazz;
    
    public BaseServiceHelper(Context context, Class<?> serviceClazz) {
        mContext = context;
        mServiceClazz = serviceClazz;
        mReceivers = new ArrayList<MessageReceiver>();
        mRequestQueue = new LinkedList<IntentHolder>();
    }

    /**
     * Register MessageReceiver.
     * @param receiver
     */
    public void addMessageReceiver(MessageReceiver receiver) {
        if(LogConstants.DEBUG) {
            Log.d(TAG, "Adding MessageReceiver: " + receiver);            
        }
        if(!mReceivers.contains(receiver)) {
            mReceivers.add(receiver);            
        }
    }

    /**
     * Unregister MessageReceiver.
     * @param receiver
     */
    public void removeMessageReceiver(MessageReceiver receiver) {
        if(LogConstants.DEBUG) {
            Log.d(TAG, "Removing MessageReceiver: " + receiver);            
        }
        if(receiver != null) {
            mReceivers.remove(receiver);            
        }
        if(mReceivers.size() == 0 && mServiceBound) {
            if(LogConstants.DEBUG) {
                Log.d(TAG, "Last receiver unregistered, trying to unbind from service");            
            }
            mContext.unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    /**
     * Receives request as intent and returns allocated rpc id.
     * @param intent
     * @return
     */
    public int handleIntent(Intent intent) {
        if(LogConstants.DEBUG) {
            Log.d(TAG, "ImageServiceHelper.handleIntent: queue intent");            
        }
        int requestId = getNextRequestId();
        mRequestQueue.add(new IntentHolder(intent, requestId));
        if(mServiceBound) {
            processRequestQueue();
        } else {
            bindService();
        }
        if(LogConstants.DEBUG) {
            Log.d(TAG, "ImageServiceHelper.handleIntent: queued with requestId=" + requestId);            
        }
        return requestId;
    }
    
    private void bindService() {
        if(LogConstants.DEBUG) {
            Log.d(TAG, "Binding Service");            
        }
        Intent intent = new Intent(mContext, mServiceClazz);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);        
    }
    
    private void processRequestQueue() {
        if(LogConstants.DEBUG) {
            Log.d(TAG, "ImageServiceHelper.processRequestQueue");            
        }
        IntentHolder i;
        boolean addedRequests = false;
        while ((i = mRequestQueue.poll()) != null) {
            mService.addRequest(i.mIntent);
            addedRequests = true;
        }
        if(addedRequests) {
            mContext.startService(new Intent(mContext, mServiceClazz));            
        }
    }
    
    /**
     * Dispatch callback data to all registered listeners.
     * @param data
     */
    private void dispatchResult(Message<?> resultData) {
        for(int i = mReceivers.size(); i>0; i--) {
            try {
                mReceivers.get(i-1).send(resultData);
            } catch (Exception e) {
                if(LogConstants.DEBUG) {
                    Log.e(TAG, "ImageServiceHelper.dispatchMindResult: error while ditpatch, remove listener.", e);            
                }
                mReceivers.remove(i-1);
            }
        }
    }
    
    /**
     * ImageService callback method.
     * @param data
     */
    protected void onInternalReceiveImageResult(Message<?> resultData) {
        dispatchResult(resultData);
    }
    
    /**
     * Gets next request id.
     * @return
     */
    private int getNextRequestId() {
        return mRequestId.incrementAndGet();
    }
    
    /**
     * Callback which is used to receive notifications from ImageService.
     */
    private MessageReceiver mMessageReceiver = new MessageReceiver() {
        @Override
        protected void onReceiveResult(Message<?> resultData) {
            onInternalReceiveImageResult(resultData);
        }        
    };
    
    /**
     * Binder interface for ImageService.
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            if(LogConstants.DEBUG) {
                Log.d(TAG, "ServiceConnection.onServiceConnected");            
            }
            GatewayServiceBinder<AbstractRequestGatewayService> binder = (GatewayServiceBinder<AbstractRequestGatewayService>) service;
            mService = binder.getService();
            mService.setReceiver(mMessageReceiver);
            mServiceBound = true;
            processRequestQueue();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(LogConstants.DEBUG) {
                Log.d(TAG, "ServiceConnection.onServiceDisconnected");            
            }
            mService = null;
            mServiceBound = false;
        }
        
    };
    
    private static class IntentHolder {
        Intent mIntent;
        Object mId;
        public IntentHolder(Intent intent, Object id) {
            mIntent = intent;
            mId = id;
        }
    }
    
}
