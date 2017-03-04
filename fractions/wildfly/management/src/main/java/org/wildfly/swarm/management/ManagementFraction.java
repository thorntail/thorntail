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
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;

import static org.wildfly.swarm.management.ManagementProperties.DEFAULT_HTTPS_PORT;
import static org.wildfly.swarm.management.ManagementProperties.DEFAULT_HTTP_PORT;
import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.integer;

/**
 * @author Bob McWhirter
 */
@MarshalDMR
@Configurable("swarm.management")
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

    public ManagementFraction securityRealm(String childKey, EnhancedSecurityRealmConsumer consumer) {
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

    public ManagementFraction httpDisable(boolean httpDisable) {
        this.httpDisable.set(httpDisable);
        return this;
    }

    public boolean isHttpDisable() {
        return this.httpDisable.get();
    }

    @Configurable("swarm.management.http.port")
    private Defaultable<Integer> httpPort = integer(DEFAULT_HTTP_PORT);

    @Configurable("swarm.management.https.port")
    private Defaultable<Integer> httpsPort = integer(DEFAULT_HTTPS_PORT);

    @Configurable("swarm.management.http.disable")
    private Defaultable<Boolean> httpDisable = bool(false);
}
