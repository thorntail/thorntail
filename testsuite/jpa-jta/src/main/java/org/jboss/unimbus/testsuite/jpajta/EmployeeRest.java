/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.unimbus.testsuite.jpajta;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * Rest service with
 *
 * @author Antoine Sabot-Durand
 */
@Transactional
@Path("/")
@RequestScoped
public class EmployeeRest {

    @Inject
    private EmployeeService service;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Transactional
    public Response create(String name) {
        Employee employee = new Employee(name);
        Employee created = service.create(employee);
        return Response.created(URI.create(created.getId().toString())).build();
    }

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String getHello() {
        return service.hello();
    }


    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Employee> get() {
        return service.getAll();
    }


    /**
     *
     * Removing with Standard Transactional behaviour (rollback)
     *
     * @param id id to fall
     * @return "ok" if id > 8
     */
    @GET
    @Path("remwrb/{failingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String remTx(@PathParam("failingId") Integer id) {
        service.remTx(id);
        return "ok";
    }

    /**
     *
     * Removing without rollback
     *
     * @param id id to fall
     * @return "ok" if id > 8
     */
    @GET
    @Path("remworb/{failingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(dontRollbackOn = {IllegalStateException.class})
    public String remTxWithNoRollbackOnISE(@PathParam("failingId") Integer id) {
        service.remTx(id);
        return "ok";
    }

    @GET
    @Path("add/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createFromGet(@PathParam("name") String name) {
        Employee employee = new Employee(name);
        Employee result = service.create(employee);
        return Response.ok(result.getId()).build();
    }

}
