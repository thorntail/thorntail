/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;

/**
 * @author Ken Finnigan
 */
public class FaviconErrorHandler implements HttpHandler {
    public FaviconErrorHandler(final HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        boolean faviconHandled = false;

        if (!exchange.isResponseComplete() && exchange.getRequestPath().contains("favicon.ico")) {

            try (InputStream faviconStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("favicon.ico")) {
                if (faviconStream != null) {
                    // Load from WAR
                    faviconHandled = writeFavicon(faviconStream, exchange);
                }
            }

            if (!faviconHandled) {
                Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.undertow", "runtime"));
                ClassLoader cl = module.getClassLoader();

                try (InputStream in = cl.getResourceAsStream("favicon.ico")) {
                    if (in != null) {
                        // Return default
                        faviconHandled = writeFavicon(in, exchange);
                    }
                }
            }
        }

        if (!faviconHandled) {
            this.next.handleRequest(exchange);
        }
    }

    private boolean writeFavicon(InputStream inputStream, HttpServerExchange exchange) throws IOException {
        if (inputStream != null) {
            exchange.startBlocking();
            OutputStream os = exchange.getOutputStream();
            byte[] buffer = new byte[1024];

            while (inputStream.read(buffer) > -1) {
                os.write(buffer);
            }

            exchange.endExchange();

            return true;
        }

        return false;
    }

    private volatile HttpHandler next;
}
