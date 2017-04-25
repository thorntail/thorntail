/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.https.test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
@DefaultDeployment
public class HttpsTest {
    @Test
    @RunAsClient
    public void hello() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial((TrustStrategy) (chain, authType) -> true)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .build()) {

            String response = Executor.newInstance(httpClient)
                    .execute(Request.Get("https://localhost:8443/"))
                    .returnContent().asString();
            assertThat(response).contains("Hello on port 8443, secure: true");
        }
    }
}
