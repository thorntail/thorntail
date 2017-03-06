/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.jaxrs;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * @author Heiko Braun
 * @since 29/03/16
 */
public class SimpleHttp {

    protected Response getUrlContents(String theUrl) {
        return getUrlContents(theUrl, true);
    }

    protected Response getUrlContents(String theUrl, boolean useAuth) {
        return getUrlContents(theUrl, useAuth, true);
    }

    protected Response getUrlContents(String theUrl, boolean useAuth, boolean followRedirects) {

        StringBuilder content = new StringBuilder();
        int code;

        try {

            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "password");
            provider.setCredentials(AuthScope.ANY, credentials);

            HttpClientBuilder builder = HttpClientBuilder.create();
            if (!followRedirects) builder.disableRedirectHandling();
            if (useAuth) builder.setDefaultCredentialsProvider(provider);
            HttpClient client = builder.build();

            HttpResponse response = client.execute(new HttpGet(theUrl));
            code = response.getStatusLine().getStatusCode();

            if (null == response.getEntity()) {
                throw new RuntimeException("No response content present");
            }

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent())
            );

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new Response(code, content.toString());
    }

    public class Response {
        public Response(int status, String body) {
            this.status = status;
            this.body = body;
        }

        public int getStatus() {
            return status;
        }

        public String getBody() {
            return body;
        }

        int status;

        String body;
    }
}
