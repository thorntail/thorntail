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
package org.wildfly.swarm.opentracing.deployment;

import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import javax.servlet.FilterRegistration.Dynamic;
import org.jboss.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.EnumSet;
import java.util.regex.Pattern;

/**
 * @author Juraci Paixão Kröhling
 */
@WebListener
public class OpenTracingInitializer implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(OpenTracingInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();

        String skipPatternAttribute = servletContext.getInitParameter(TracingFilter.SKIP_PATTERN);
        if (null != skipPatternAttribute && !skipPatternAttribute.isEmpty()) {
            servletContext.setAttribute(TracingFilter.SKIP_PATTERN, Pattern.compile(skipPatternAttribute));
        }

        logger.info("Registering Tracing Filter");
        Dynamic filterRegistration = servletContext
            .addFilter("tracingFilter", new TracingFilter());
        filterRegistration.setAsyncSupported(true);
        filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
