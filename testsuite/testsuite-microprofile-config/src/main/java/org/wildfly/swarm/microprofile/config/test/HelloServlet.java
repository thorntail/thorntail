/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.config.test;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.wildfly.swarm.microprofile.config.test.ServiceDefinedConfigSource.PROP_FROM_CONFIG_SOURCE_FROM_SERVICE;
import static org.wildfly.swarm.microprofile.config.test.YamlDefinedConfigSource.PROP_FROM_CONFIG_SOURCE_FROM_YAML;

/**
 * @author Juan Gonzalez
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 */

@WebServlet("/")
public class HelloServlet extends HttpServlet {
    public static final String COMMA = "\",";

    @Inject
    @ConfigProperty(name = "app.timeout")
    private long appTimeout;

    @Inject
    @ConfigProperty(name = "prop.from.config.source")
    private String propFromConfigSource;
    @Inject
    @ConfigProperty(name = PROP_FROM_CONFIG_SOURCE_FROM_SERVICE)
    private String propFromConfigSourceFromService;
    @Inject
    @ConfigProperty(name = PROP_FROM_CONFIG_SOURCE_FROM_YAML)
    private String propFromConfigSourceFromYaml;

    @Inject
    @ConfigProperty(name = "missing.property", defaultValue = "it's present anyway")
    private String missingProperty;

    @Inject
    private Config config;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().println("{");
        resp.getWriter().println("\"timeout\": " + appTimeout + ",");
        resp.getWriter().println("\"optionalTimeoutFromConfig\": " +
                config.getOptionalValue("app.timeout", Integer.class).orElse(-1) + ",");
        resp.getWriter().println("\"propFromConfigSource\": \"" + propFromConfigSource + COMMA);
        resp.getWriter().println("\"propFromConfigSourceFromService\": \"" + propFromConfigSourceFromService + COMMA);
        resp.getWriter().println("\"propFromConfigSourceFromYaml\": \"" + propFromConfigSourceFromYaml + COMMA);
        resp.getWriter().println("\"missingProperty\": \"" + missingProperty + "\"");
        resp.getWriter().println("}");
        resp.setContentType("application/json");
    }
}
