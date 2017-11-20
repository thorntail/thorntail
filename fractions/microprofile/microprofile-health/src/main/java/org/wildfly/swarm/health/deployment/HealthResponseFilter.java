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
package org.wildfly.swarm.health.deployment;

import org.wildfly.swarm.health.Status;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
public class HealthResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {

        if (resp.hasEntity() && (resp.getEntity() instanceof Status)) {
            Status status = (Status) resp.getEntity();
            int code = (Status.State.UP == status.getState()) ? 200 : 503;
            resp.setStatus(code);
            resp.setEntity(status.toJson());
            resp.getHeaders().putSingle("Content-Type", MediaType.APPLICATION_JSON);
            resp.getHeaders().add("Access-Control-Allow-Origin", "*");
            resp.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
            resp.getHeaders().add("Access-Control-Allow-Credentials", "true");
            resp.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            resp.getHeaders().add("Access-Control-Max-Age", "1209600");
        }
    }

}

