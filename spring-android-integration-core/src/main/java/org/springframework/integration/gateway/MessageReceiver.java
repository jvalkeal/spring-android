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

import org.springframework.integration.Message;

import android.os.Handler;

/**
 * Common method to handle callback pattern with a spring integration
 * {@link Message}s. This class can be used to handle direct callback or
 * callback routed through given {@link Handler}.
 * 
 * @author Janne Valkealahti
 */
public class MessageReceiver {

    final Handler mHandler;

    /**
     * Construct message receiver without Handler.
     */
    public MessageReceiver() {
        this(null);
    }

    /**
     * Construct message receiver with Handler.
     * 
     * @param handler The handler
     */
    public MessageReceiver(Handler handler) {
        mHandler = handler;
    }

    /**
     * Sends spring integration {@link Message} to interested party.
     * 
     * @param message the message
     */
    public void send(Message<?> message) {
        if (mHandler != null) {
            mHandler.post(new PosterRunnable(message));
        } else {
            onReceiveResult(message);
        }
    }

    /**
     * Override to receive results delivered to this object.
     * 
     * @param resultCode
     *            Arbitrary result code delivered by the sender, as defined by
     *            the sender.
     * @param resultData
     *            Any additional data provided by the sender.
     */
    protected void onReceiveResult(Message<?> message) {
        // space for rent
    }

    /**
     * Simple runnable to call {@link MessageReceiver#onReceiveResult(Message)}.
     */
    private class PosterRunnable implements Runnable {
        final Message<?> mResultData;

        PosterRunnable(Message<?> resultData) {
            mResultData = resultData;
        }

        public void run() {
            onReceiveResult(mResultData);
        }
    }

}
