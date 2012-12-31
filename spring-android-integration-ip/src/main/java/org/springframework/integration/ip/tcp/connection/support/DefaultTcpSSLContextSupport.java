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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.springframework.util.Assert;

/**
 * Default implementation of {@link TcpSSLContextSupport}; uses a
 * 'TLS' (by default) {@link SSLContext}, initialized with default
 * Key and Trust managers in an underlying SSL implementation
 * respectively.
 * 
 * @author Gary Russell
 * @author Janne Valkealahti
 *
 */
public class DefaultTcpSSLContextSupport implements TcpSSLContextSupport {

    private KeyManager[] mKeyManagers;
    
    private TrustManager[] mTrustManagers;
    
    private SecureRandom mSecureRandom;

	private volatile String protocol = "TLS";	
	
	public DefaultTcpSSLContextSupport(KeyManager[] km, TrustManager[] tm, SecureRandom sr) {
	    mKeyManagers = km;
	    mTrustManagers = tm;
	    mSecureRandom = sr;
	}
	
	@Override
    public SSLContext getSSLContext() throws GeneralSecurityException, IOException  {
        SSLContext sslContext = SSLContext.getInstance(protocol);
        // passing nulls to init should enable defaults
        sslContext.init(mKeyManagers, mTrustManagers, mSecureRandom);
        return sslContext;
	}

	/**
	 * The protocol used in {@link SSLContext#getInstance(String)}; default "TLS".
	 * @param protocol The protocol.
	 */
	public void setProtocol(String protocol) {
		Assert.notNull(protocol, "protocol must not be null");
		this.protocol = protocol;
	}

}
