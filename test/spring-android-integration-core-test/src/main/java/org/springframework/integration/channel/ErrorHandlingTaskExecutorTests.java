/*
 * Copyright 2002-2010 the original author or authors.
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
package org.springframework.integration.channel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.util.ErrorHandlingTaskExecutor;
import org.springframework.util.ErrorHandler;

import android.test.AndroidTestCase;

/**
 * Testing error handling.
 * 
 * @author Janne Valkealahti
 */
public class ErrorHandlingTaskExecutorTests extends AndroidTestCase {

    
    public void testVerifyErrorHandled() throws Exception {
        
        CountDownLatch errorLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(1);
        
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setThreadNamePrefix("test-");
        
        TestErrorHandler errorHandler = new TestErrorHandler(errorLatch);        
        ErrorHandlingTaskExecutor errorTaskExecutor = new ErrorHandlingTaskExecutor(taskExecutor, errorHandler);
        
        ExecutorChannel channel = new ExecutorChannel(errorTaskExecutor);
        
        TestHandler handler = new TestHandler(messageLatch);
        handler.shouldFail = true;
        
        channel.subscribe(handler);
        channel.send(new GenericMessage<String>("test"));
        errorLatch.await(1000, TimeUnit.MILLISECONDS);
        messageLatch.await(1000, TimeUnit.MILLISECONDS);
        
        assertEquals(0, errorLatch.getCount());
        assertEquals(0, messageLatch.getCount());
        assertNotNull(handler.thread);
        assertFalse(Thread.currentThread().equals(handler.thread));
        assertEquals("test-1", handler.thread.getName());
    }
    
    private static class TestErrorHandler implements ErrorHandler {

        private final CountDownLatch latch;
        private final AtomicInteger count = new AtomicInteger();
        
        public TestErrorHandler(CountDownLatch latch) {
            this.latch = latch;
        }
        
        @Override
        public void handleError(Throwable t) {
            this.count.incrementAndGet();
            this.latch.countDown();
        }
        
    }

    private static class TestHandler implements MessageHandler {

        private final CountDownLatch latch;
        private final AtomicInteger count = new AtomicInteger();
        private volatile Thread thread;
        private volatile boolean shouldFail;

        public TestHandler(CountDownLatch latch) {
            this.latch = latch;
        }

        public void handleMessage(Message<?> message) {
            this.thread = Thread.currentThread();
            this.count.incrementAndGet();
            this.latch.countDown();
            if (this.shouldFail) {
                throw new RuntimeException("intentional test failure");
            }
        }
    }
    
}
