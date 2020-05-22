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
package org.wildfly.swarm.undertow.runtime;


import java.security.NoSuchAlgorithmException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * A {@link Customizer} implementation to enable HTTP/2
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Post
@ApplicationScoped
public class HTTP2Customizer implements Customizer {

    @Inject
    UndertowFraction undertow;

    @Override
    public void customize() {
        if (!supportsHTTP2()) {
            SwarmMessages.MESSAGES.http2NotSupported();
            return;
        }
        for (Server server : undertow.subresources().servers()) {
            server.subresources().httpsListeners().forEach(httpsListener -> httpsListener.enableHttp2(true));
        }
    }

    public static final String REQUIRED_CIPHER =        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";
    public static final String REQUIRED_CIPHER_IBMJDK = "SSL_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

    protected boolean supportsHTTP2() {
        try {
            SSLContext context = SSLContext.getDefault();
            SSLEngine engine = context.createSSLEngine();
            String[] ciphers = engine.getEnabledCipherSuites();
            for (String i : ciphers) {
                if (REQUIRED_CIPHER.equals(i) || REQUIRED_CIPHER_IBMJDK.equals(i)) {
                    return true;
                }
            }
        } catch (NoSuchAlgorithmException e) {
        }
        return false;
    }

}
