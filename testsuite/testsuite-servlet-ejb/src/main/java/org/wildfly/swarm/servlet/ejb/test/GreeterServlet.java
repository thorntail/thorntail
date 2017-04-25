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
package org.wildfly.swarm.servlet.ejb.test;

import java.io.IOException;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/hello")
public class GreeterServlet extends HttpServlet {
    @EJB
    private SimpleService simpleService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        GreeterService greeter = (GreeterService) session.getAttribute(GreeterService.class.getName());
        if (greeter == null) {
            try {
                greeter = InitialContext.doLookup("java:module/" + GreeterService.class.getSimpleName());
                session.setAttribute(GreeterService.class.getName(), greeter);
            } catch (NamingException e) {
                throw new ServletException(e);
            }
        }

        String name = req.getParameter("name");
        resp.getWriter().println(simpleService.yay() + greeter.hello(name));
    }
}
