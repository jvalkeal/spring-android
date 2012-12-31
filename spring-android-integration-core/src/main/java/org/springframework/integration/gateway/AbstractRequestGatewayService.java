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

import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Intent;
import android.util.Log;

/**
 * Extension to add a request queue functionality on top of
 * {@link AbstractGatewayService}. It makes things a bit faster when adding
 * requests via binded service.
 * 
 * @author Janne Valkealahti
 */
public abstract class AbstractRequestGatewayService extends AbstractGatewayService {

    private final static String TAG = "AbstractRequestGatewayService";
    private ConcurrentLinkedQueue<Intent> mRequestQueue;

    public AbstractRequestGatewayService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestQueue = new ConcurrentLinkedQueue<Intent>();
    }

    /**
     * Add intent to a request queue.
     * @param intent the Intent to add
     */
    public void addRequest(Intent intent) {
        mRequestQueue.add(intent);
    }

    /**
     * Processes the current request queue. Flushed intents from a queue are
     * simply passed to {@link #processIntent(Intent)} method.
     */
    protected void processRequestQueue() {
        Intent i;
        while ((i = mRequestQueue.poll()) != null) {
            try {
                processIntent(i);
            } catch (Exception e) {
                Log.e(TAG, "Error processing intent from queue.", e);
            }
        }
    }

    /**
     * Subclasses should implement this method to process
     * queued intents.
     * @param i intent to process
     */
    protected abstract void processIntent(Intent i);

}
