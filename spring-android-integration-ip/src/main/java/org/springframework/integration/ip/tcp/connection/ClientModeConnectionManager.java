/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.integration.ip.tcp.connection;

import org.springframework.integration.ip.LogConstants;
import org.springframework.util.Assert;

import android.util.Log;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.util.Assert;

/**
 * Intended to be run on a schedule, simply gets the connection
 * from a client connection factory each time it is run.
 * If no connection exists (or it has been closed), the
 * connection factory will create a new one (if possible).
 *
 * @author Gary Russell
 *
 */
public class ClientModeConnectionManager implements Runnable {

    private final static String TAG = "ClientModeConnectionManager";

	private final AbstractConnectionFactory clientConnectionFactory;

	private volatile TcpConnection lastConnection;

	/**
	 * @param clientConnectionFactory
	 */
	public ClientModeConnectionManager(
			AbstractConnectionFactory clientConnectionFactory) {
		Assert.notNull(clientConnectionFactory, "Connection factory cannot be null");
		this.clientConnectionFactory = clientConnectionFactory;
	}

	public void run() {
		synchronized (this.clientConnectionFactory) {
			try {
				TcpConnection connection = this.clientConnectionFactory.getConnection();
				if (connection != lastConnection) {
				    if(LogConstants.DEBUG) {
				        Log.d(TAG, "Connection " + connection.getConnectionId() + " established");
				    }
					lastConnection = connection;
				} else {
				    if(LogConstants.VERBOSE) {
				        Log.v(TAG, "Connection " + connection.getConnectionId() + " still OK");
				    }
				}
			} catch (Exception e) {
			    Log.e(TAG, "Could not establish connection using " + this.clientConnectionFactory, e);
			}
		}
	}

	public boolean isConnected() {
		return this.lastConnection == null ? false : this.lastConnection.isOpen();
	}

}
