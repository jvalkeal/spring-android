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

package org.springframework.integration.ip.tcp.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import javax.net.SocketFactory;

import org.springframework.integration.ip.LogConstants;
import org.springframework.integration.ip.tcp.connection.support.DefaultTcpNetSocketFactorySupport;
import org.springframework.integration.ip.tcp.connection.support.TcpSocketFactorySupport;
import org.springframework.util.Assert;

import android.util.Log;


/**
 * A client connection factory that creates {@link TcpNetConnection}s.
 * @author Gary Russell
 *
 */
public class TcpNetClientConnectionFactory extends
		AbstractClientConnectionFactory {
    
    private final static String TAG = "TcpNetClientConnectionFactory";

	private volatile TcpSocketFactorySupport tcpSocketFactorySupport = new DefaultTcpNetSocketFactorySupport();

	/**
	 * Creates a TcpNetClientConnectionFactory for connections to the host and port.
	 * @param host the host
	 * @param port the port
	 */
	public TcpNetClientConnectionFactory(String host, int port) {
		super(host, port);
	}

	/**
	 * @throws IOException
	 * @throws SocketException
	 * @throws Exception
	 */
	@Override
	protected TcpConnection obtainConnection() throws Exception {
		TcpConnection theConnection = this.getTheConnection();
		if (theConnection != null && theConnection.isOpen()) {
			return theConnection;
		}
		if (LogConstants.DEBUG) {
			Log.d(TAG, "Opening new socket connection to " + this.getHost() + ":" + this.getPort());
		}
		Socket socket = createSocket(this.getHost(), this.getPort());
		setSocketAttributes(socket);
		TcpConnection connection = new TcpNetConnection(socket, false, this.isLookupHost());
		connection = wrapConnection(connection);
		initializeConnection(connection, socket);
		this.getTaskExecutor().execute(connection);
		this.harvestClosedConnections();
		return connection;
	}

	@Override
	public void start() {
		this.setActive(true);
		super.start();
	}

	/**
	 * Create a new {@link Socket}. This default implementation uses the default
	 * {@link SocketFactory}. Override to use some other mechanism
	 *
	 * @param host The host.
	 * @param port The port.
	 * @return The Socket
	 * @throws IOException
	 */
	protected Socket createSocket(String host, int port) throws IOException {
		return this.tcpSocketFactorySupport.getSocketFactory().createSocket(host, port);
	}

	@Override
	public void close() {
	}

	protected TcpSocketFactorySupport getTcpSocketFactorySupport() {
		return tcpSocketFactorySupport;
	}

	public void setTcpSocketFactorySupport(TcpSocketFactorySupport tcpSocketFactorySupport) {
		Assert.notNull(tcpSocketFactorySupport, "TcpSocketFactorySupport may not be null");
		this.tcpSocketFactorySupport = tcpSocketFactorySupport;
	}

}
