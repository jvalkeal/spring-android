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

package org.springframework.cache.disk.support;

import android.os.Environment;
import android.os.StatFs;

/**
 * Utility methods for handling storage in Android.
 * 
 * @author Janne Valkealahti
 */
public class StorageUtils {

    public static long cachePartitionAvailableSpace() {
        StatFs statFs = new StatFs(Environment.getDownloadCacheDirectory().getPath());
        return statFs.getAvailableBlocks() * statFs.getBlockSize();
    }

    public static long dataPartitionAvailableSpace() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        return statFs.getAvailableBlocks() * statFs.getBlockSize();
    }

    public static boolean externalStorageAvailable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public static long externalStorageAvailableSpace() {
        long l = -1L;
        if (!externalStorageAvailable()) {
            return l;
        }
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return statFs.getAvailableBlocks() * statFs.getBlockSize();
    }

    public static String getFilenameHashForKey(String key) {
        int i = key.length() / 2;
        String str = String.valueOf(key.substring(0, i).hashCode());
        return str + String.valueOf(key.substring(i).hashCode());
    }

}
