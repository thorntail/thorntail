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
package org.wildfly.swarm.undertow.internal;

import javax.servlet.ServletContext;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

/**
 * @author Ken Finnigan
 */
public class FaviconServletExtension implements ServletExtension {
    public static final String HANDLER_NAME = "org.wildfly.swarm.generated.FaviconErrorHandler";

    public static final String EXTENSION_NAME = "org.wildfly.swarm.generated.FaviconServletExtension";

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        deploymentInfo.addInitialHandlerChainWrapper(handler -> {
            try {
                return Handlers.exceptionHandler((HttpHandler) Class.forName(HANDLER_NAME).getConstructor(HttpHandler.class).newInstance(handler));
            } catch (Exception e) {
                e.printStackTrace();
                return handler;
            }
        });
    }
}
