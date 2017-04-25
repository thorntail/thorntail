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
package org.wildfly.swarm.jaxrs.ejb.test;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

@Path("/hello")
@Stateless
public class GreeterResource {
    @EJB
    private SimpleService simpleService;

    @GET
    public String hello(@QueryParam("name") String name, @Context HttpServletRequest request) throws NamingException {
        HttpSession session = request.getSession();
        GreeterService greeter = (GreeterService) session.getAttribute(GreeterService.class.getName());
        if (greeter == null) {
            greeter = InitialContext.doLookup("java:module/" + GreeterService.class.getSimpleName());
            session.setAttribute(GreeterService.class.getName(), greeter);
        }

        return simpleService.yay() + greeter.hello(name);
    }
}
