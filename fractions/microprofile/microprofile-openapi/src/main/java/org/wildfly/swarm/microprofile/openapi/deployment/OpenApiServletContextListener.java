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
package org.wildfly.swarm.microprofile.openapi.deployment;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.wildfly.swarm.microprofile.openapi.api.OpenApiDocument;
import org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl;

/**
 * This listener instantiates OASModelReader and OASFilter and triggers OpenAPI document initialization.
 *
 * @author Martin Kouba
 */
public class OpenApiServletContextListener implements ServletContextListener {

    private final Config config;

    public OpenApiServletContextListener() {
        this(ConfigProvider.getConfig());
    }

    public OpenApiServletContextListener(Config config) {
        this.config = config;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Instantiate OASModelReader and OASFilter
        OpenApiDocument openApiDocument = OpenApiDocument.INSTANCE;
        openApiDocument.modelFromReader(modelFromReader());
        openApiDocument.filter(getFilter());
        // Now we're ready to initialize the final model
        openApiDocument.initialize();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    /**
     * Instantiate the configured {@link OASModelReader} and invoke it. If no reader is configured, then return null. If a class is configured but there is an
     * error either instantiating or invokig it, a {@link RuntimeException} is thrown.
     */
    private OpenAPIImpl modelFromReader() {
        String readerClassName = config.getOptionalValue(OASConfig.MODEL_READER, String.class).orElse(null);
        if (readerClassName == null) {
            return null;
        }
        try {
            ClassLoader cl = getContextClassLoader();
            if (cl == null) {
                cl = OpenApiServletContextListener.class.getClassLoader();
            }
            Class<?> c = cl.loadClass(readerClassName);
            OASModelReader reader = (OASModelReader) c.newInstance();
            return (OpenAPIImpl) reader.buildModel();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiate the {@link OASFilter} configured by the app.
     */
    private OASFilter getFilter() {
        String filterClassName = config.getOptionalValue(OASConfig.FILTER, String.class).orElse(null);
        if (filterClassName == null) {
            return null;
        }
        try {
            ClassLoader cl = getContextClassLoader();
            if (cl == null) {
                cl = OpenApiServletContextListener.class.getClassLoader();
            }
            Class<?> c = cl.loadClass(filterClassName);
            return (OASFilter) c.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
    }
}
