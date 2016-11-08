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
package org.wildfly.swarm.management;

import javax.annotation.PostConstruct;

import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.management.HTTPInterfaceManagementInterfaceConsumer;
import org.wildfly.swarm.spi.api.Configurable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;

import static org.wildfly.swarm.management.ManagementProperties.DEFAULT_HTTPS_PORT;
import static org.wildfly.swarm.management.ManagementProperties.DEFAULT_HTTP_PORT;
import static org.wildfly.swarm.spi.api.Configurable.integer;
import static org.wildfly.swarm.spi.api.SwarmProperties.HTTP_PORT;

/**
 * @author Bob McWhirter
 */
@MarshalDMR
public class ManagementFraction extends ManagementCoreService<ManagementFraction> implements Fraction<ManagementFraction> {

    public ManagementFraction() {

    }

    public static ManagementFraction createDefaultFraction() {
        return new ManagementFraction().applyDefaults();
    }

    @PostConstruct
    public void postConstruct() {
        applyDefaults();
    }

    public ManagementFraction applyDefaults() {
        httpInterfaceManagementInterface();
        return this;
    }

    @Override
    public ManagementFraction httpInterfaceManagementInterface() {
        return httpInterfaceManagementInterface((iface) -> {
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public ManagementFraction httpInterfaceManagementInterface(HTTPInterfaceManagementInterfaceConsumer consumer) {
        return super.httpInterfaceManagementInterface((iface) -> {
            iface.consoleEnabled(false);
            iface.httpUpgradeEnabled(true);
            iface.socketBinding("management-http");
            consumer.accept(iface);
        });
    }

    public ManagementFraction securityRealm(String childKey, EnhancedSecurityRealm.Consumer consumer) {
        return securityRealm(() -> {
            EnhancedSecurityRealm realm = new EnhancedSecurityRealm(childKey);
            consumer.accept(realm);
            return realm;
        });
    }

    public ManagementFraction httpPort(int port) {
        this.httpPort.set(port);
        return this;
    }

    public int httpPort() {
        return this.httpPort.get();
    }

    public ManagementFraction httpsPort(int port) {
        this.httpsPort.set(port);
        return this;
    }

    public int httpsPort() {
        return this.httpsPort.get();
    }

    private Configurable<Integer> httpPort = integer("swarm.management.http.port", DEFAULT_HTTP_PORT);

    private Configurable<Integer> httpsPort = integer("swarm.management.https.port", DEFAULT_HTTPS_PORT);
}
