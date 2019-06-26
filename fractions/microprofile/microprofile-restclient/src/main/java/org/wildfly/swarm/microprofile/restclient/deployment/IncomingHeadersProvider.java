/**
 * Copyright 2015-2019 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.restclient.deployment;

import org.jboss.resteasy.specimpl.UnmodifiableMultivaluedMap;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 2/21/19
 */
public class IncomingHeadersProvider implements org.jboss.resteasy.microprofile.client.header.IncomingHeadersProvider {


    public static final UnmodifiableMultivaluedMap<String, String> EMPTY_MAP =
            new UnmodifiableMultivaluedMap<>(new MultivaluedHashMap<>());

    /**
     * @return headers incoming in the JAX-RS request, if any
     */
    @Override
    public MultivaluedMap<String, String> getIncomingHeaders() {
        HttpRequest request = ResteasyProviderFactory.getContextData(HttpRequest.class);
        if (request != null) {
            return request.getHttpHeaders().getRequestHeaders();
        } else {
            return EMPTY_MAP;
        }
    }
}
