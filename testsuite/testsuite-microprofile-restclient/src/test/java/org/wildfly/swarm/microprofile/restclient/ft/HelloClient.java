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
package org.wildfly.swarm.microprofile.restclient.ft;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

@Path("/v1")
@Produces(MediaType.TEXT_PLAIN)
public interface HelloClient {

    @Retry(maxRetries = 4)
    @GET
    @Path("/hello")
    String helloRetry();

    @Fallback(DummyFallbackHandler.class)
    @GET
    @Path("/hello")
    String helloFallback();

    @Fallback(fallbackMethod = "fallback")
    @GET
    @Path("/hello")
    String helloFallbackDefaultMethod();

    @GET // resteasy client proxies do not ignore default methods, see also RESTEASY-798 fixed in 3.5.1.Final
    default String fallback() {
        return "defaultFallback";
    }

    // Circuit should be open after 2 requests
    @CircuitBreaker(requestVolumeThreshold = 2)
    @GET
    @Path("/hello")
    String helloCircuitBreaker();

    @Timeout(200)
    @GET
    @Path("/hello")
    String helloTimeout();

    // Note that bulkhead does not make sense without ResteasyClientBuilder.connectionPoolSize()
    @Bulkhead(FaultToleranceTest.BULKHEAD)
    @Fallback(fallbackMethod = "bulkheadFallback")
    @GET
    @Path("/helloBulk")
    String helloBulkhead(@QueryParam("wait") boolean wait);

    @GET // resteasy client proxies do not ignore default methods, see also RESTEASY-798 fixed in 3.5.1.Final
    default String bulkheadFallback(boolean wait) {
        return "bulkheadFallback";
    }

}