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

import java.util.concurrent.Executor;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.channel.AbstractSubscribableChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.message.ErrorMessage;
import org.springframework.integration.util.ErrorHandlingTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

import android.content.Intent;
import android.util.Log;

/**
 * Adding functionality to handle messages asynchronously via
 * separate executors on inbound and outbound channels.
 * 
 * @author Janne Valkealahti
 */
public abstract class AbstractAsyncGatewayService extends AbstractRequestGatewayService {

    private final static String TAG = "AbstractAsyncGatewayService";
    
    private AbstractSubscribableChannel mOutChannel;
    private AbstractSubscribableChannel mInChannel;
    private Executor mInExecutor;
    private Executor mOutExecutor;
    
    public AbstractAsyncGatewayService(String name) {
        super(name);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();

        // we need to have different executors for inbound and outbound,
        // otherwise burst of messages to channel might starve a single executor
        // if there's a limit. using an executor with unlimited threads will most
        // likely result dalvik crash.
        mInExecutor = initInboundExecutor();
        mOutExecutor = initOutboundExecutor();
                
        DispatchErrorHandler inErrorHandler = new DispatchErrorHandler();
        ErrorHandlingTaskExecutor inErrorTaskExecutor = new ErrorHandlingTaskExecutor(mInExecutor, inErrorHandler);
        mInChannel = new ExecutorChannel(inErrorTaskExecutor);
        mInChannel.subscribe(new DispatchHandler());        
        onInboundChannelPostProcess(mInChannel);
        
        DispatchErrorHandler outErrorHandler = new DispatchErrorHandler();
        ErrorHandlingTaskExecutor outErrorTaskExecutor = new ErrorHandlingTaskExecutor(mOutExecutor, outErrorHandler);
        mOutChannel = new ExecutorChannel(outErrorTaskExecutor);
        onOutboundChannelPostProcess(mOutChannel);
        
        // register message handlers for outbound channel
        AbstractReplyProducingMessageHandler[] messageHandlers = getMessageHandlers();
        Assert.noNullElements(messageHandlers, "There can't be null message handlers.");
        for(AbstractReplyProducingMessageHandler handler : messageHandlers) {
            handler.setOutputChannel(mInChannel);
            mOutChannel.subscribe(handler);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // clear out local resources and threads
        // TODO: this is not a good way to clear out executors,
        //       we'd need a proper way to destroy bean just like
        //       spring is handling it. what about if executor is
        //       not a instance of DisposableBean?
        if(mInExecutor instanceof DisposableBean) {
            try {
                ((DisposableBean)mInExecutor).destroy();
            } catch (Exception e) {
                Log.e(TAG, "Error shutting executor", e);
            }
        }
        if(mOutExecutor instanceof DisposableBean) {
            try {
                ((DisposableBean)mOutExecutor).destroy();
            } catch (Exception e) {
                Log.e(TAG, "Error shutting executor", e);
            }
        }
    }
    
    protected void sendToInbound(Message<?> message) {
        mInChannel.send(message);
    }
    
    /**
     * Callback for outbound channel to possibly modify it after
     * channel is being initialised. Default implementation will
     * not do anything.
     * @param channel the channel to modify.
     */
    protected void onOutboundChannelPostProcess(AbstractSubscribableChannel channel) {        
    }

    /**
     * Callback for inbound channel to possibly modify it after
     * channel is being initialised. Default implementation will
     * not do anything.
     * @param channel the channel to modify.
     */
    protected void onInboundChannelPostProcess(AbstractSubscribableChannel channel) {        
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        addRequest(intent);
        processRequestQueue();
    }

    @Override
    protected void processIntent(Intent i) {
        Message<?> message = createMessage(i);
        if(message != null) {
            mOutChannel.send(message);
        }
    }
    
    /**
     * Initialise executor for inbound channel. Default implementation
     * is a {@link ThreadPoolTaskExecutor} with default settings.
     * @return the Executor 
     */
    protected Executor initInboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        return executor;
    }

    /**
     * Initialise executor for outbound channel. Default implementation
     * is a {@link ThreadPoolTaskExecutor} with default settings.
     * @return the Executor 
     */
    protected Executor initOutboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        return executor;
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
     * Implementation is calling this method to init a message handlers.
     * @return the message handlers
     */
    protected abstract AbstractReplyProducingMessageHandler[] getMessageHandlers();
    
    /**
     * Simple message handler which dispatches messages from
     * a channel to a registered callback.
     */
    private class DispatchHandler implements MessageHandler {
        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            dispatchResult(message);
        }        
    }
    
    /**
     * Simple handler for dispatching errors.
     */
    private class DispatchErrorHandler implements ErrorHandler {
        @Override
        public void handleError(Throwable t) {
            dispatchResult(new ErrorMessage(t));
        }        
    }

}
