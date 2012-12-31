package org.springframework.integration.image;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.integration.image.BitmapManager.BitmapWrapper;
import org.springframework.integration.image.core.CachingBitmapManager;
import org.springframework.integration.image.support.ImageOptions;

import android.content.Context;
import android.graphics.Bitmap;
import android.test.AndroidTestCase;

/**
 * 
 * @author Janne Valkealahti
 */
public class CachingBitmapManagerTests extends AndroidTestCase {

    public void testSimpleCaching() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        
        TestBitmapManager manager = new TestBitmapManager(getContext());
        
        ImageOptions options = new ImageOptions.Builder()
            .withUrl("http://www.springsource.org/sites/all/themes/s2org11/images/logo_springsource_community.png")
            .build();
        
        manager.registerBitmapObserver(new BitmapObserver() {
            @Override
            public void onBitmap(Bitmap bitmap, Map<String, String> tags) {
                latch.countDown();
            }
        });

        BitmapWrapper wrapper = manager.requestBitmap(options);
        // wrapper should not be null
        assertNotNull(wrapper);
        // we requested image from network, so wrapped bitmap should be null
        // as we're waiting for the callback to give us the actual bitmap
        assertNull(wrapper.get());
        
        latch.await(10, TimeUnit.SECONDS);
        assertEquals(0l, latch.getCount());

        // it should be cached now
        wrapper = manager.requestBitmap(options);
        assertNotNull(wrapper.get());
        
    }
    
    private class TestBitmapManager extends CachingBitmapManager {
        public TestBitmapManager(Context context) {
            super(context);
        }        
        @Override
        protected Cache initCache() {
            return new ConcurrentMapCache("test");
        }        
    }

}
