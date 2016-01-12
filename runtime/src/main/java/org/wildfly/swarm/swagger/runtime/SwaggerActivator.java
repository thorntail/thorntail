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
package org.wildfly.swarm.swagger.runtime;

import io.swagger.jaxrs.config.BeanConfig;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceRegistryException;
import org.wildfly.swarm.swagger.SwaggerArchive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Lance Ball
 */
public class SwaggerActivator implements ServiceActivator {
    @Override
    public void activate(ServiceActivatorContext serviceActivatorContext) throws ServiceRegistryException {
        System.err.println(">>>>> IN ACTIVATOR");

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(SwaggerArchive.SWAGGER_CONFIGURATION_PATH);

        if (in == null) {
            return;
        }
        String apiVersion = System.getProperty("swarm.swagger.api.version", "1.0.0");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            BeanConfig beanConfig = new BeanConfig();

            // TODO: Make all of these configurable via SWAGGER_CONFIGURATION_PATH
            beanConfig.setVersion(apiVersion);
            beanConfig.setSchemes(new String[]{"http"});
            beanConfig.setHost("localhost:8080");
            beanConfig.setBasePath("/swagger");
            beanConfig.setScan(true);

            String packageName;
            while ((packageName = reader.readLine()) != null) {
                packageName = packageName.trim();
                if (!packageName.isEmpty()) {
                    beanConfig.setResourcePackage(packageName);
                }
            }
        } catch (IOException e) {
            throw new ServiceRegistryException(e);
        }
    }
}
