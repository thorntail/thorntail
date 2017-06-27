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
package org.wildfly.swarm.jaeger.deployment;

import com.uber.jaeger.Configuration;
import io.opentracing.util.GlobalTracer;
import org.jboss.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import static com.uber.jaeger.Configuration.*;

/**
 * @author Juraci Paixão Kröhling
 */
@WebListener
public class JaegerInitializer implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(JaegerInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext sc = servletContextEvent.getServletContext();

        String serviceName = getProperty(sc, JAEGER_SERVICE_NAME);
        if (serviceName == null || serviceName.isEmpty()) {
            logger.info("No Service Name set. Skipping initialization of the Jaeger Tracer.");
            return;
        }

        Configuration configuration = new Configuration(
                getProperty(sc, JAEGER_SERVICE_NAME),
                new Configuration.SamplerConfiguration(
                        getProperty(sc, JAEGER_SAMPLER_TYPE),
                        getPropertyAsInt(sc, JAEGER_SAMPLER_PARAM),
                        getProperty(sc, JAEGER_SAMPLER_MANAGER_HOST_PORT)),
                new ReporterConfiguration(
                        getPropertyAsBoolean(sc, JAEGER_REPORTER_LOG_SPANS),
                        getProperty(sc, JAEGER_AGENT_HOST),
                        getPropertyAsInt(sc, JAEGER_AGENT_PORT),
                        getPropertyAsInt(sc, JAEGER_REPORTER_FLUSH_INTERVAL),
                        getPropertyAsInt(sc, JAEGER_REPORTER_MAX_QUEUE_SIZE)
                )
        );
        GlobalTracer.register(configuration.getTracer());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    private static String getProperty(ServletContext sc, String name) {
        return sc.getInitParameter(name);
    }

    private static Integer getPropertyAsInt(ServletContext sc, String name) {
        String value = getProperty(sc, name);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.error("Failed to parse integer for property '" + name + "' with value '" + value + "'", e);
            }
        }
        return null;
    }

    private static Boolean getPropertyAsBoolean(ServletContext sc, String name) {
        String value = getProperty(sc, name);
        if (value != null && !value.isEmpty()) {
            return Boolean.valueOf(value);
        }
        return null;
    }

}
