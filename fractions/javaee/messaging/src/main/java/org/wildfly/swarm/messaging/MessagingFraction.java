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
package org.wildfly.swarm.messaging;

import org.wildfly.swarm.config.MessagingActiveMQ;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
@WildFlyExtension(module = "org.wildfly.extension.messaging-activemq")
@MarshalDMR
@DeploymentModule(name = "javax.jms.api")
@Configurable("thorntail.messaging-activemq")
public class MessagingFraction extends MessagingActiveMQ<MessagingFraction> implements Fraction<MessagingFraction> {

    public MessagingFraction() {

    }

    /**
     * Create a fraction with the default local server.
     *
     * @return This fraction.
     */

    public static MessagingFraction createDefaultFraction() {
        return new MessagingFraction().applyDefaults();
    }

    public MessagingFraction applyDefaults() {
        return defaultServer();
    }

    /**
     * Create a fraction and configure the default local server.
     *
     * @param config The configurator.
     * @return This fraction.
     */
    public static MessagingFraction createDefaultFraction(EnhancedServerConsumer config) {
        return new MessagingFraction().defaultServer(config);
    }

    /**
     * Create the default local server if required.
     *
     * @return This fraction.
     */
    public MessagingFraction defaultServer() {
        findOrCreateDefaultServer();

        return this;
    }

    /**
     * Configure the default local server, creating it first if required.
     *
     * @param config The configurator.
     * @return This fraction.
     */
    public MessagingFraction defaultServer(EnhancedServerConsumer config) {
        config.accept(findOrCreateDefaultServer());

        return this;
    }

    /**
     * Configure a named server.
     *
     * @param childKey The key (name) of the server.
     * @param config   The configurator.
     * @return This fraction.
     */
    public MessagingFraction server(String childKey, EnhancedServerConsumer config) {
        super.server(() -> {
            final EnhancedServer s = new EnhancedServer(childKey);
            config.accept(s);

            return s;
        });

        return this;
    }

    private EnhancedServer findOrCreateDefaultServer() {
        EnhancedServer server = (EnhancedServer) subresources().server("default");
        if (server == null) {
            server("default", EnhancedServer::enableInVm);
        }

        return (EnhancedServer) subresources().server("default");
    }
}

