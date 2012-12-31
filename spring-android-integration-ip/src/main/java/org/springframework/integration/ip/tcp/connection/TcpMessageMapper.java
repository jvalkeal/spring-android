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

package org.springframework.integration.ip.tcp.connection;

import java.io.UnsupportedEncodingException;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.mapping.InboundMessageMapper;
import org.springframework.integration.mapping.OutboundMessageMapper;
import org.springframework.integration.support.MessageBuilder;


/**
 * Maps incoming data from a {@link TcpConnection} to a {@link Message}.
 * If StringToBytes is true (default),
 * payloads of type String are converted to a byte[] using the supplied
 * charset (UTF-8 by default).
 * Inbound messages include headers representing the remote end of the
 * connection as well as a connection id that can be used by a {@link TcpSender}
 * to correlate which connection to send a reply.
 * @author Gary Russell
 *
 */
public class TcpMessageMapper implements
		InboundMessageMapper<TcpConnection>,
		OutboundMessageMapper<Object> {

	private volatile String charset = "UTF-8";

	private volatile boolean stringToBytes = true;

	private volatile boolean applySequence = false;

	public Message<Object> toMessage(TcpConnection connection) throws Exception {
		Message<Object> message = null;
		Object payload = connection.getPayload();
		if (payload != null) {
			String connectionId = connection.getConnectionId();
			if (this.applySequence) {
				message = MessageBuilder.withPayload(payload)
						.setHeader(IpHeaders.HOSTNAME, connection.getHostName())
						.setHeader(IpHeaders.IP_ADDRESS, connection.getHostAddress())
						.setHeader(IpHeaders.REMOTE_PORT, connection.getPort())
						.setHeader(IpHeaders.CONNECTION_ID, connectionId)
						.setCorrelationId(connectionId)
						.setSequenceNumber((int) connection.incrementAndGetConnectionSequence())
						.build();
			} else {
				message = MessageBuilder.withPayload(payload)
						.setHeader(IpHeaders.HOSTNAME, connection.getHostName())
						.setHeader(IpHeaders.IP_ADDRESS, connection.getHostAddress())
						.setHeader(IpHeaders.REMOTE_PORT, connection.getPort())
						.setHeader(IpHeaders.CONNECTION_ID, connectionId)
						.build();
			}
		}
		return message;

	}

	public Object fromMessage(Message<?> message) throws Exception {
		if (this.stringToBytes) {
			return getPayloadAsBytes(message);
		}
		return message.getPayload();
	}

	/**
	 * Extracts the payload as a byte array.
	 * @param message
	 * @return
	 */
	private byte[] getPayloadAsBytes(Message<?> message) {
		byte[] bytes = null;
		Object payload = message.getPayload();
		if (payload instanceof byte[]) {
			bytes = (byte[]) payload;
		}
		else if (payload instanceof String) {
			try {
				bytes = ((String) payload).getBytes(this.charset);
			}
			catch (UnsupportedEncodingException e) {
				throw new MessageHandlingException(message, e);
			}
		}
		else {
			throw new MessageHandlingException(message,
					"When using a byte array serializer, the socket mapper expects " +
					"either a byte array or String payload, but received: " + payload.getClass());
		}
		return bytes;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Sets whether outbound String payloads are to be converted
	 * to byte[]. Default is true.
	 * @param stringToBytes
	 */
	public void setStringToBytes(boolean stringToBytes) {
		this.stringToBytes = stringToBytes;
	}

	/**
	 * @param applySequence the applySequence to set
	 */
	public void setApplySequence(boolean applySequence) {
		this.applySequence = applySequence;
	}

}
