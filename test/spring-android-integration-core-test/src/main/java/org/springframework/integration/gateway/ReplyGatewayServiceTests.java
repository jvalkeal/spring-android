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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.integration.Message;
import org.springframework.integration.gateway.AbstractGatewayService.GatewayServiceBinder;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

/**
 * Tests for AbstractReplyGatewayService.
 * 
 * @author Janne Valkealahti
 */
public class ReplyGatewayServiceTests extends ServiceTestCase<TestReplyGatewayService> {

    public ReplyGatewayServiceTests() {
        super(TestReplyGatewayService.class);
    }
    
    public void testSimpleRequestReply() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        
        // intent for both bind and start
        Intent intent = new Intent(getContext(), TestReplyGatewayService.class);
        
        // bind so that we can set message receiver
        IBinder binder = bindService(intent);
        GatewayServiceBinder<TestReplyGatewayService> serviceBinder = (GatewayServiceBinder<TestReplyGatewayService>) binder;
        TestReplyGatewayService gatewayService = serviceBinder.getService();
        
        // we can't set handler for MessageReceiver - new MessageReceiver(new Handler())
        // test won't work with it, instead rely on direct callback
        // TODO: should really understand why handler doesn't work in this case!
        gatewayService.setReceiver(new MessageReceiver() {
            @Override
            protected void onReceiveResult(Message<?> resultData) {
                latch.countDown();
            }
        });
        
        // start service so that it will go into processing loop
        startService(intent);
        
        // wait for callback. AbstractReplyGatewayService is based on IntentService
        // and processing should happen in separate thread which calls receiver.
        latch.await(10, TimeUnit.SECONDS);
        assertEquals(0l, latch.getCount());
    }
        
}
