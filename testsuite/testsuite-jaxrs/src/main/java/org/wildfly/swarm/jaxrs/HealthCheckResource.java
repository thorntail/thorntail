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

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.wildfly.swarm.monitor.Health;
import org.wildfly.swarm.monitor.HealthStatus;

/**
 * @author Heiko Braun
 */
@Path("/app")
public class HealthCheckResource {


    @GET
    @Health
    @Path("/health-secure")
    public HealthStatus checkHealth() {
        return HealthStatus.up().withAttribute("status", "UP");
    }

    @GET
    @Health(inheritSecurity = false)
    @Path("/health-insecure")
    public HealthStatus checkHealthInsecure() {
        return HealthStatus.up().withAttribute("status", "UP");
    }
}
