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

package org.springframework.android.support.v4.cache.disk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Various testing utils and helper methods.
 * 
 * @author Janne Valkealahti
 *
 */
public class TestUtils {

    /**
     * Reads inputstream and returns content as String.
     */
    public static String readFileAsString(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();        
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        
        return builder.toString();
    }

    public static void removeAllFiles(File dir) {
        for(File f : dir.listFiles()) {
            f.delete();
        }
    }
    
    

}
