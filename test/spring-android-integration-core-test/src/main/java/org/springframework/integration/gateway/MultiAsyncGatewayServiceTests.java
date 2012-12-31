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
 * 
 * @author Janne Valkealahti
 */
public class MultiAsyncGatewayServiceTests extends ServiceTestCase<TestMultiHandlerMultiThreadAsyncGatewayService> {

    public MultiAsyncGatewayServiceTests() {
        super(TestMultiHandlerMultiThreadAsyncGatewayService.class);
    }
        
    public void testComplexRequestReply() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(21);        
        Intent intent = new Intent(getContext(), TestMultiHandlerMultiThreadAsyncGatewayService.class);
        
        IBinder binder = bindService(intent);
        GatewayServiceBinder<TestMultiHandlerMultiThreadAsyncGatewayService> serviceBinder = 
                (GatewayServiceBinder<TestMultiHandlerMultiThreadAsyncGatewayService>) binder;
        TestMultiHandlerMultiThreadAsyncGatewayService gatewayService = serviceBinder.getService();
        
        gatewayService.setReceiver(new MessageReceiver() {
            @Override
            protected void onReceiveResult(Message<?> resultData) {
                latch.countDown();
            }
        });
        
        for(int i = 0; i<20; i++) {
            gatewayService.addRequest(intent);
        }
        
        startService(intent);
        
        latch.await(10, TimeUnit.SECONDS);
        assertEquals(0l, latch.getCount());
    }
    

}
