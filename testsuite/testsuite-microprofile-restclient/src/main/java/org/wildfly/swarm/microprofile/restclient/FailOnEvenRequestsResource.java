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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 9/27/18
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FailOnEvenRequestsResource {

    private static AtomicInteger requestNo = new AtomicInteger(0);

    @GET
    public Response get() {
        if (requestNo.incrementAndGet() % 2 == 0) {
            return Response.serverError().entity("no-json").build();
        } else {
            BooleanValue result = new BooleanValue();
            result.setContent(true);
            return Response.ok(result).build();
        }
    }

    @DELETE
    public void reset() {
        requestNo.set(0);
    }


}
