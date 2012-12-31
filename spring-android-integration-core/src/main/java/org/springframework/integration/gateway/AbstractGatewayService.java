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

import org.springframework.integration.LogConstants;
import org.springframework.integration.Message;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Base gateway implementation handling service binding
 * and {@link MessageReceiver}.
 * 
 * @author Janne Valkealahti
 */
public abstract class AbstractGatewayService extends IntentService {

    private final static String TAG = "AbstractGatewayService";
    
    private boolean mBound = false;
    private MessageReceiver mReceiver;
    private final IBinder mBinder = new GatewayServiceBinder<AbstractGatewayService>();
    
    public AbstractGatewayService(String name) {
        super(name);
        mReceiver = null;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        if(LogConstants.DEBUG) {
            Log.d(TAG, "onBind: " + intent + " / " + this);
        }
        mBound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(LogConstants.DEBUG) {
            Log.d(TAG, "onUnbind: " + intent + " / " + this);
        }
        mBound = false;
        return super.onUnbind(intent);
    }
    
    /**
     * Sets a {@link MessageReceiver} for this instance. 
     * Will overwrite previous receiver if set.
     * @param receiver the message receiver to set
     */
    public void setReceiver(MessageReceiver receiver) {
        mReceiver = receiver;
    }
    
    /**
     * Gets current {@link MessageReceiver}.
     * @return the message receiver
     */
    protected MessageReceiver getReceiver() {
        return mReceiver;
    }

    /**
     * Dispatch a {@link Message} to registered
     * {@link MessageReceiver}. Message is silently
     * discarded if message receiver is not set.
     * @param data the message to dispatch.
     */
    protected void dispatchResult(Message<?> data) {
        if(mReceiver != null) {
            mReceiver.send(data);
        } else if(LogConstants.DEBUG) {
            Log.w(TAG, "Dispatching result but message receiver is null");
        }
    }
    
    /**
     * Returns if this service is bounded.
     * @return True if bounded, false otherwise
     */
    public boolean isBounded() {
        return mBound;
    }
    
    /**
     * Generic binder implementation so that we're aware of it
     * in parent {@link AbstractGatewayService}.
     * @param <T> type of the implementor
     */
    public class GatewayServiceBinder<T extends AbstractGatewayService> extends Binder {
        @SuppressWarnings("unchecked")
        public T getService() {
            return (T) AbstractGatewayService.this;
        }
    }
    
}
