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
import java.util.concurrent.Executors;

import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import android.content.Intent;

/**
 * 
 * @author Janne Valkealahti
 */
public class TestMultiHandlerMultiThreadAsyncGatewayService extends AbstractAsyncGatewayService {

    public TestMultiHandlerMultiThreadAsyncGatewayService() {
        super("TestMultiHandlerMultiThreadAsyncGatewayService");
    }

    @Override
    protected Message<?> createMessage(Intent intent) {
        return MessageBuilder.withPayload("dummy").build();
    }

    @Override
    protected AbstractReplyProducingMessageHandler[] getMessageHandlers() {
        return new AbstractReplyProducingMessageHandler[]{new MockMessageHandler(),new MockMessageHandler(),new MockMessageHandler()};
    }
    
    @Override
    protected Executor initInboundExecutor() {
        ConcurrentTaskExecutor executor = new ConcurrentTaskExecutor(
                Executors.newFixedThreadPool(3, new CustomizableThreadFactory("test-")));
        return executor;        
    }

    @Override
    protected Executor initOutboundExecutor() {
        ConcurrentTaskExecutor executor = new ConcurrentTaskExecutor(
                Executors.newFixedThreadPool(3, new CustomizableThreadFactory("test-")));
        return executor;        
    }
    
    private class MockMessageHandler extends AbstractReplyProducingMessageHandler {

        @Override
        protected Object handleRequestMessage(Message<?> requestMessage) {
            return requestMessage;
        }
        
    }
    
}
