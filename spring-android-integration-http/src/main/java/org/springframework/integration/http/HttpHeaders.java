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

package org.springframework.integration.http;

/**
 * @author Mark Fisher
 * @author Janne Valkealahti
 */
public abstract class HttpHeaders {

	private static final String PREFIX = "http_";

	public static final String REQUEST_URL = PREFIX + "requestUrl";

	public static final String REQUEST_METHOD = PREFIX + "requestMethod";

	public static final String USER_PRINCIPAL = PREFIX + "userPrincipal";

	public static final String STATUS_CODE = PREFIX + "statusCode";
	
    public static final String URI_VARIABLES = PREFIX + "uriVariables";

    public static final String RESPONSE_TYPE = PREFIX + "responseType";
    
}
