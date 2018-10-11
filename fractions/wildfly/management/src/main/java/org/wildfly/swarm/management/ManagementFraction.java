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
package org.wildfly.swarm.management;

import javax.annotation.PostConstruct;

import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.management.HTTPInterfaceManagementInterfaceConsumer;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
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
@Configurable("thorntail.management")
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

    public static final String JSON_FORMATTER = "json-formatter";

    public static final String AUDIT_LOG_FILE = "audit-log.log";

    public static final String FILE_HANDLER = "file";

    public ManagementFraction applyDefaults() {
        httpInterfaceManagementInterface();

        auditAccess((access) -> {
            access.jsonFormatter(JSON_FORMATTER, (formatter) -> {
            });
            access.fileHandler(FILE_HANDLER, (handler) -> {
                handler.formatter(JSON_FORMATTER);
                handler.path(AUDIT_LOG_FILE);
                handler.relativeTo("user.dir"); // WildFly defaults to jboss.server.data.dir, but that's in /tmp for us
            });
            access.auditLogLogger((logger) -> {
                logger.logBoot(true);
                logger.logReadOnly(false);
                logger.enabled(false);
                logger.handler(FILE_HANDLER);
            });
        });

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
            iface.httpUpgrade("enabled", true);
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

    public ManagementFraction enableDefaultAuditAccess() {
        subresources().auditAccess().subresources().auditLogLogger().enabled(true);
        return this;
    }

    @AttributeDocumentation("Port for HTTP access to management interface")
    @Configurable("thorntail.management.http.port")
    private Defaultable<Integer> httpPort = integer(DEFAULT_HTTP_PORT);

    @AttributeDocumentation("Port for HTTPS access to management interface")
    @Configurable("thorntail.management.https.port")
    private Defaultable<Integer> httpsPort = integer(DEFAULT_HTTPS_PORT);

    @AttributeDocumentation("Flag to disable HTTP access to management interface")
    @Configurable("thorntail.management.http.disable")
    private Defaultable<Boolean> httpDisable = bool(false);
}
