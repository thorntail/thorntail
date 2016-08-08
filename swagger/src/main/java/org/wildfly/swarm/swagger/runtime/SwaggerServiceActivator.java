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

import java.io.InputStream;

import io.swagger.jaxrs.config.BeanConfig;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceRegistryException;
import org.wildfly.swarm.swagger.SwaggerArchive;
import org.wildfly.swarm.swagger.SwaggerConfig;

/**
 * @author Lance Ball
 */

public class SwaggerServiceActivator implements ServiceActivator {
    @Override
    public void activate(ServiceActivatorContext serviceActivatorContext) throws ServiceRegistryException {

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(SwaggerArchive.SWAGGER_CONFIGURATION_PATH);


        if (in == null) {

            // No config available. Print a warning and return
            System.err.println("WARN: No swagger configuration found. Swagger not activated.");
            return;
        }

        SwaggerConfig config = new SwaggerConfig(in);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setHost((String) config.get(SwaggerConfig.Key.HOST));
        beanConfig.setLicense((String) config.get(SwaggerConfig.Key.LICENSE));
        beanConfig.setLicenseUrl((String) config.get(SwaggerConfig.Key.LICENSE_URL));
        beanConfig.setTermsOfServiceUrl((String) config.get(SwaggerConfig.Key.TERMS_OF_SERVICE_URL));

        // some type inconsistencies in the API (String vs String[])
        String[] packages = (String[]) config.get(SwaggerConfig.Key.PACKAGES);

        if(packages!=null) {
            StringBuffer sb = new StringBuffer();
            for (String s : packages) {
                sb.append(s).append(',');
            }

            beanConfig.setResourcePackage(sb.toString());
        }

        beanConfig.setVersion((String) config.get(SwaggerConfig.Key.VERSION));
        beanConfig.setBasePath((String) config.get(SwaggerConfig.Key.ROOT));
        beanConfig.setContact((String) config.get(SwaggerConfig.Key.CONTACT));
        beanConfig.setDescription((String) config.get(SwaggerConfig.Key.DESCRIPTION));
        beanConfig.setTitle((String) config.get(SwaggerConfig.Key.TITLE));
        beanConfig.setPrettyPrint((String) config.get(SwaggerConfig.Key.PRETTY_PRINT));
        beanConfig.setSchemes((String[]) config.get(SwaggerConfig.Key.SCHEMES));

        beanConfig.setScan(true);

    }
}
