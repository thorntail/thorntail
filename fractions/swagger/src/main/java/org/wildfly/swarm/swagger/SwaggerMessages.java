/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.swarm.swagger;

import java.util.Collection;
import java.util.List;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "WFSSWGR", length = 4)
public interface SwaggerMessages extends BasicLogger {

    SwaggerMessages MESSAGES = Logger.getMessageLogger(SwaggerMessages.class, "org.wildfly.swarm.swagger");

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1, value = "No swagger configuration found; Swagger not activated.")
    void noConfigurationFound();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 2, value = "Ignoring package: %s")
    void ignoringPackage(String pkg);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 3, value = "No eligible packages in deployment: %s")
    void noEligiblePackages(String deployment);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 4, value = "Configure Swagger for deployment %s with package %s")
    void configureSwaggerForPackage(String deployment, String pkg);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 5, value = "Configure Swagger for deployment %s with packages %s")
    void configureSwaggerForSeveralPackages(String deployment, List<String> packages);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 6, value = "Multiple application paths found for REST application: %s")
    void multipleApplicationPathsFound(Collection<String> paths);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 7, value = "Adding Swagger resources to JAX-RS deployment.")
    void addingSwaggerResourcesToCustomApplicationSubClass();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 8, value = "Swagger interceptor added incorrectly to bean with type: %s")
    void warnInvalidBeanTarget(Class<?> beanType);
}
