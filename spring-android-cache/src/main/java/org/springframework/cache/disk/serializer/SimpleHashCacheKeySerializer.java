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

package org.springframework.cache.disk.serializer;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple serializer which digests the the object and creates an ascii
 * encoded string out from 'SHA-1' algorithm. Ascii encoding will use
 * characters from a set '0123456789abcdef'. Example of a final String
 * is something like '0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33'.
 * 
 * @author Janne Valkealahti
 */
public class SimpleHashCacheKeySerializer implements CacheKeySerializer<String> {

    public SimpleHashCacheKeySerializer() {
    }
    
    @Override
    public String serialize(String object) throws SerializationException {
        try {
            // MessageDigest is not thread safe
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] digestBytes = digest.digest(object.getBytes("UTF-8"));
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < digestBytes.length; i++) {
                buf.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return buf.toString();
        } catch (UnsupportedEncodingException e) {
            throw new SerializationException("Unable to serialize key", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SerializationException("Could not find digest instance", e);
        }
    }
    
}
