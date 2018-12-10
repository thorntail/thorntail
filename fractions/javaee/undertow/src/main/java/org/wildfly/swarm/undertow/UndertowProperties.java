/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.undertow;

/**
 * @author Bob McWhirter
 */
public interface UndertowProperties {
    int DEFAULT_HTTP_PORT = 8080;
    int DEFAULT_HTTPS_PORT = 8443;
    int DEFAULT_AJP_PORT = 8009;

    String DEFAULT_KEYSTORE_PATH = "application.keystore";
    String DEFAULT_KEYSTORE_PASSWORD = "password";
    String DEFAULT_KEY_PASSWORD = "password";
    String DEFAULT_CERTIFICATE_ALIAS = "server";

    String DEFAULT_SERVER = "default-server";
    String DEFAULT_HTTP_LISTENER = "default";
    String DEFAULT_HTTPS_LISTENER = "default-https";
    String DEFAULT_HOST = "default-host";
    String DEFAULT_SERVLET_CONTAINER = "default";
    String DEFAULT_BUFFER_CACHE = "default";
}
