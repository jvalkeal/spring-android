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
import org.springframework.integration.support.MessageBuilder;

import android.content.Intent;

/**
 * 
 * @author Janne Valkealahti
 */
public class TestReplyGatewayService extends AbstractReplyGatewayService {

    public TestReplyGatewayService() {
        super("TestReplyGatewayService");
    }

    @Override
    protected Message<?> createMessage(Intent intent) {
        return MessageBuilder.withPayload("dummy").build();
    }

    @Override
    protected AbstractPollableChannel getChannel() {
        return new MockChannel();
    }

    @Override
    protected AbstractReplyProducingMessageHandler getMessageHandler() {
        return new MockMessageHandler();
    }

    private class MockChannel extends AbstractPollableChannel {

        @Override
        protected Message<?> doReceive(long timeout) {
            return MessageBuilder.withPayload("dummy").build();
        }

        @Override
        protected boolean doSend(Message<?> message, long timeout) {
            return true;
        }
        
    }
    
    private class MockMessageHandler extends AbstractReplyProducingMessageHandler {

        @Override
        protected Object handleRequestMessage(Message<?> requestMessage) {
            return requestMessage;
        }
        
    }

}
