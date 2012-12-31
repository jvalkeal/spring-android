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

package org.springframework.integration.ip.tcp;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.ip.LogConstants;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractConnectionFactory;
import org.springframework.integration.ip.tcp.connection.ClientModeConnectionManager;
import org.springframework.integration.ip.tcp.connection.ConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpSender;
import org.springframework.integration.mapping.MessageMappingException;

//import junit.framework.Assert;

import android.util.Log;


/**
 * Tcp outbound channel adapter using a TcpConnection to
 * send data. If it is a client factory, this object owns the connection.
 * @author Gary Russell
 *
 */
public class TcpSendingMessageHandler extends AbstractMessageHandler implements	TcpSender/*, SmartLifecycle, ClientModeCapable*/ {
    
    private final static String TAG = "TcpSendingMessageHandler";    

	private volatile AbstractConnectionFactory clientConnectionFactory;

	private Map<String, TcpConnection> connections = new ConcurrentHashMap<String, TcpConnection>();

	private volatile boolean autoStartup = true;

	private volatile int phase;

	private volatile boolean isClientMode;

	private volatile long retryInterval = 60000;

	private volatile ScheduledFuture<?> scheduledFuture;

	private volatile ClientModeConnectionManager clientModeConnectionManager;

	protected final Object lifecycleMonitor = new Object();

	private volatile boolean active;

	protected TcpConnection getConnection() {
		TcpConnection connection = null;
		if (this.clientConnectionFactory == null) {
			return null;
		}
		try {
			connection = this.clientConnectionFactory.getConnection();
		} catch (Exception e) {
			Log.e(TAG, "Error creating SocketWriter", e);
		}
		return connection;
	}

	/**
	 * Writes the message payload to the underlying socket, using the specified
	 * message format.
	 * @see org.springframework.integration.core.MessageHandler#handleMessage(org.springframework.integration.Message)
	 */
	@Override
	public void handleMessageInternal(final Message<?> message) throws
			MessageHandlingException {

		// we own the connection
		try {
			doWrite(message);
		} catch (MessageMappingException e) {
			// retry - socket may have closed
			if (e.getCause() instanceof IOException) {
                if (LogConstants.DEBUG) {
                    Log.d(TAG, "Fail on first write attempt", e);
                }
				doWrite(message);
			} else {
				throw e;
			}
		}
	}

	/**
	 * Method that actually does the write.
	 * @param message The message to write.
	 */
	protected void doWrite(Message<?> message) {
		TcpConnection connection = null;
		try {
			connection = getConnection();
			if (connection == null) {
				throw new MessageMappingException(message, "Failed to create connection");
			}
            if (LogConstants.DEBUG) {
                Log.d(TAG, "Got Connection " + connection.getConnectionId());
            }
			connection.send(message);
		} catch (Exception e) {
			String connectionId = null;
			if (connection != null) {
				connectionId = connection.getConnectionId();
			}
			if (e instanceof MessageMappingException) {
				throw (MessageMappingException) e;
			}
			throw new MessageMappingException(message, "Failed to map message using " + connectionId, e);
		}
	}

	/**
	 * Sets the client or server connection factory; for this (an outbound adapter), if
	 * the factory is a server connection factory, the sockets are owned by a receiving
	 * channel adapter and this adapter is used to send replies.
	 *
	 * @param connectionFactory the connectionFactory to set
	 */
	public void setConnectionFactory(AbstractConnectionFactory connectionFactory) {
		if (connectionFactory instanceof AbstractClientConnectionFactory) {
			this.clientConnectionFactory = connectionFactory;
		}
	}

	public void addNewConnection(TcpConnection connection) {
		connections.put(connection.getConnectionId(), connection);
	}

	public void removeDeadConnection(TcpConnection connection) {
		connections.remove(connection.getConnectionId());
	}

	public void start() {
		synchronized (this.lifecycleMonitor) {
			if (!this.active) {
				this.active = true;
				if (this.clientConnectionFactory != null) {
					this.clientConnectionFactory.start();
				}
				if (this.isClientMode) {
					ClientModeConnectionManager manager = new ClientModeConnectionManager(
							this.clientConnectionFactory);
					this.clientModeConnectionManager = manager;
//					Assert.state(this.getTaskScheduler() != null, "Client mode requires a task scheduler");
//					this.scheduledFuture = this.getTaskScheduler().scheduleAtFixedRate(manager, this.retryInterval);
				}
			}
		}
	}

	public void stop() {
		synchronized (this.lifecycleMonitor) {
			if (this.active) {
				this.active = false;
				if (this.scheduledFuture != null) {
					this.scheduledFuture.cancel(true);
				}
				if (this.clientConnectionFactory != null) {
					this.clientConnectionFactory.stop();
				}
			}
		}
	}

	public boolean isRunning() {
		return this.active;
	}

	public int getPhase() {
		return this.phase;
	}

	public boolean isAutoStartup() {
		return this.autoStartup;
	}

	public void stop(Runnable callback) {
		synchronized (this.lifecycleMonitor) {
			if (this.active) {
				this.active = false;
				if (this.scheduledFuture != null) {
					this.scheduledFuture.cancel(true);
				}
				this.clientModeConnectionManager = null;
				if (this.clientConnectionFactory != null) {
					this.clientConnectionFactory.stop(callback);
				}
			}
		}
	}

	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	/**
	 * @return the clientConnectionFactory
	 */
	protected ConnectionFactory getClientConnectionFactory() {
		return clientConnectionFactory;
	}

	/**
	 * @return the connections
	 */
	protected Map<String, TcpConnection> getConnections() {
		return connections;
	}

	/**
	 * @return the isClientMode
	 */
	public boolean isClientMode() {
		return this.isClientMode;
	}

	/**
	 * @param isClientMode
	 *            the isClientMode to set
	 */
	public void setClientMode(boolean isClientMode) {
		this.isClientMode = isClientMode;
	}

	/**
	 * @param scheduler the scheduler to set
	 * @deprecated Use {@link #setTaskScheduler(TaskScheduler)}
	 */
//	@Deprecated
//	public void setScheduler(TaskScheduler scheduler) {
//		this.setTaskScheduler(scheduler);
//	}

//	@Override // super class is protected
//	public void setTaskScheduler(TaskScheduler taskScheduler) {
//		super.setTaskScheduler(taskScheduler);
//	}

	/**
	 * @return the retryInterval
	 */
	public long getRetryInterval() {
		return this.retryInterval;
	}

	/**
	 * @param retryInterval
	 *            the retryInterval to set
	 */
	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}

	public boolean isClientModeConnected() {
		if (this.isClientMode && this.clientModeConnectionManager != null) {
			return this.clientModeConnectionManager.isConnected();
		} else {
			return false;
		}
	}

	public void retryConnection() {
		if (this.active && this.isClientMode && this.clientModeConnectionManager != null) {
			this.clientModeConnectionManager.run();
		}
	}

}
