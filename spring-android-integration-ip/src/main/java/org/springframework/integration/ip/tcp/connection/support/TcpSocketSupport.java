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

package org.springframework.integration.ip.tcp.connection.support;

import java.net.Socket;

/**
 * Strategy interface for modifying sockets.
 * @author Gary Russell
 *
 */
public interface TcpSocketSupport {

    /**
     * Performs any further modifications to the {@link Socket} after
     * the socket has been created by a client, or accepted by
     * a server, and after any configured atributes have been
     * set.
     * @param socket The Socket
     */
    void postProcessSocket(Socket socket);

}
