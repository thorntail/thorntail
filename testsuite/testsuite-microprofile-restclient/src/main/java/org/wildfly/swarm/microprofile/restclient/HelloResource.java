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
package org.wildfly.swarm.microprofile.restclient;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

@Path("/")
public class HelloResource {

    @Inject
    private Counter counter;

    @Inject
    private Timer timer;

    @Inject
    private Latch latch;

    @GET
    @Produces("text/plain")
    @Path("/hello")
    public String hello() {
        try {
            timer.sleep();
            if (counter.incrementAndTest()) {
                return "OK" + counter.getCount();
            } else {
                throw new WebApplicationException(500);
            }
        } finally {
            latch.countDown();
        }
    }

    @GET
    @Produces("text/plain")
    @Path("/helloBulk")
    public String helloBulk(@QueryParam("wait") boolean wait) throws InterruptedException {
        // We use the default latch as "start"
        latch.countDown();
        if (wait && !latch.await("end")) {
            throw new WebApplicationException("End latch not counted down", 500);
        }
        return "OK";
    }
}