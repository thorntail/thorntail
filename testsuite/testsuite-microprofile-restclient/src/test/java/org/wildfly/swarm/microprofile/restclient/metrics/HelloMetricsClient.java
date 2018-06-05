/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.restclient.metrics;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

@Path("/v1")
public interface HelloMetricsClient {

    static final String TIMED_NAME = "hello-time";

    static final String COUNTED_NAME = "hello-count";

    @Timed(unit = MetricUnits.MILLISECONDS, name = TIMED_NAME, absolute = true)
    @GET
    @Path("/hello")
    String helloTimed();

    @Counted(name = COUNTED_NAME, absolute = true, monotonic = true)
    @GET
    @Path("/hello")
    String helloCounted();

}