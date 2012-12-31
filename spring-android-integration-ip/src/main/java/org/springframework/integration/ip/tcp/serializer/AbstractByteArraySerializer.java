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

package org.springframework.integration.ip.tcp.serializer;

import java.io.IOException;

import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.integration.ip.LogConstants;

import android.util.Log;


/**
 * Base class for (de)serializers that provide a mechanism to 
 * reconstruct a byte array from an arbitrary stream.
 * 
 * @author Gary Russell
 *
 */
public abstract class AbstractByteArraySerializer implements Serializer<byte[]>, Deserializer<byte[]> {

    private final static String TAG = "AbstractByteArraySerializer";
    
	protected int maxMessageSize = 2048;
	
	/**
	 * The maximum supported message size for this serializer.
	 * Default 2048.
	 * @return The max message size.
	 */
	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	/**
	 * The maximum supported message size for this serializer.
	 * Default 2048.
	 * @param maxMessageSize The max message size.
	 */
	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	protected void checkClosure(int bite) throws IOException {
		if (bite < 0) {
		    if(LogConstants.DEBUG) Log.d(TAG, "Socket closed during message assembly");
			throw new IOException("Socket closed during message assembly");
		}
	}

}
