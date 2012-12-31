package org.springframework.integration.image;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.integration.image.BitmapManager.BitmapWrapper;
import org.springframework.integration.image.support.ImageOptions;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;
import android.util.Log;

public class DirectNetworkBitmapManagerTests extends AndroidTestCase {

    public void testSingleImageOverHttpNoImageProcessing() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        
        DirectNetworkBitmapManager manager = new DirectNetworkBitmapManager(getContext());
        
        ImageOptions options = new ImageOptions.Builder()
            .withUrl("http://www.springsource.org/sites/all/themes/s2org11/images/logo_springsource_community.png")
            .addTag("foo", "jee")
            .build();
        
        manager.registerBitmapObserver(new BitmapObserver() {
            @Override
            public void onBitmap(Bitmap bitmap, Map<String, String> tags) {
                Log.d("Foo2", "onBitmap: " + bitmap);
                if(tags != null) {
                    for(Entry<String, String> entry : tags.entrySet()) {
                        Log.d("Foo2", "onBitmap tag: " + entry.getKey() + " / " +entry.getValue());                    
                    }
                } else {
                    Log.d("Foo2", "onBitmap: no tags");                    
                }
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
        
    }
        
}
