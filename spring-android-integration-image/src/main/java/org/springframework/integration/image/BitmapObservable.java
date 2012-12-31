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

package org.springframework.integration.image;

import java.util.Map;

import android.database.Observable;
import android.graphics.Bitmap;

/**
 * A specialization of {@link Observable} for {@link BitmapObserver}
 * that provides methods for sending notifications to a list of
 * {@link BitmapObserver} objects.
 * 
 * @author Janne Valkealahti
 */
public class BitmapObservable extends Observable<BitmapObserver> {

    /**
     * Invokes {@link BitmapObserver#onBitmap(Bitmap, String)} on each observer.
     * Called when the bitmap is changed.
     */
    public void notifyBitmap(Bitmap bitmap, Map<String, String> tags) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onBitmap(bitmap, tags);
            }
        }
    }
    
    /**
     * Checks if there are reqistered observers.
     * @return True if there are registered observers, false otherwise.
     */
    public boolean hasObservers() {
        return !mObservers.isEmpty();
    }
    
}
