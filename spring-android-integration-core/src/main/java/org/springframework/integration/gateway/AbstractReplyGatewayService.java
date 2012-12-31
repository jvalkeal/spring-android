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
import org.springframework.integration.channel.AbstractPollableChannel;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;

import android.content.Intent;

/**
 * Base gateway implementation which uses a request/reply
 * pattern using a pollable channel and message handler which
 * posts request results back to a channel. 
 * 
 * @author Janne Valkealahti
 */
public abstract class AbstractReplyGatewayService extends AbstractRequestGatewayService {

    private AbstractPollableChannel mChannel;
    private AbstractReplyProducingMessageHandler mMessageHandler;
    private int mPollTimeout;
    
    public AbstractReplyGatewayService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mChannel = getChannel();
        mMessageHandler = getMessageHandler();
        mMessageHandler.setOutputChannel(mChannel);
        mPollTimeout = getPollTimeout();
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        addRequest(intent);
        processRequestQueue();
    }
    
    /**
     * Default implementation is just calling abstract 
     * {@ #createMessage(Intent)} method and then passes
     * returned message to a message handler. After message
     * has been processed by a hander, output channel will
     * be polled and received message dispatched to message
     * listener.
     */
    protected void processIntent(Intent i) {
        Message<?> message = createMessage(i);
        if(message != null) {
            mMessageHandler.handleMessage(message);
            Message<?> response = mChannel.receive(mPollTimeout);
            dispatchResult(response);                    
        }
    }
    
    /**
     * Implementation will use this method to set a timeout
     * for output channel poll operation. Default is 10 seconds.
     * @return the timeout in milliseconds.
     */
    protected int getPollTimeout() {
        return 10000;
    }
    
    /**
     * Implementor needs to use this method to do a conversion
     * from an Android {@link Intent} to a Spring Integration
     * {@link Message}.
     * @param intent the intent to convert
     * @return Converted Spring Integration message
     */
    protected abstract Message<?> createMessage(Intent intent);
    
    /**
     * Channel returned from this method will be set
     * as a output channel for message handler returned
     * from method {@ #getMessageHandler()}.
     * @return the pollable channel
     */
    protected abstract AbstractPollableChannel getChannel();
    
    /**
     * Implementation is calling this method to init a message
     * handler capable of producing messages back to a reply channel.
     * @return the message handler
     */
    protected abstract AbstractReplyProducingMessageHandler getMessageHandler();
    
}
