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
package org.wildfly.swarm.teiid.runtime;

import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.integer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.config.teiid.Transport;
import org.wildfly.swarm.container.runtime.config.DefaultSocketBindingGroupProducer;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.teiid.TeiidFraction;

@Pre
@ApplicationScoped
public class TransportCustomizer implements Customizer {

    @Configurable("thorntail.teiid.jdbc.port")
    @AttributeDocumentation("JDBC Connection Port")
    private Defaultable<Integer> jdbcPort = integer(31000);

    @Configurable("thorntail.teiid.odbc.port")
    @AttributeDocumentation("ODBC Connection Port")
    private Defaultable<Integer> odbcPort = integer(35432);

    @Configurable("thorntail.teiid.jdbc.enable")
    @AttributeDocumentation("Enable JDBC Connections")
    private Defaultable<Boolean> enableJDBC = bool(true);

    @Configurable("thorntail.teiid.odbc.enable")
    @AttributeDocumentation("Enable ODBC Connections")
    private Defaultable<Boolean> enableODBC = bool(false);

    @Inject
    @Named(DefaultSocketBindingGroupProducer.STANDARD_SOCKETS)
    private SocketBindingGroup group;

    @Inject
    TeiidFraction fraction;

    @Override
    public void customize() {
        this.fraction.transport("local");
        this.fraction.transport("odata");

        if (enableJDBC.get()) {
            this.group.socketBinding(new SocketBinding("teiid-jdbc").port(jdbcPort.get()));
            this.fraction.transport("jdbc", t -> t.socketBinding("teiid-jdbc").protocol(Transport.Protocol.TEIID));
        }
        if (enableODBC.get()) {
            this.group.socketBinding(new SocketBinding("teiid-odbc").port(odbcPort.get()));
            this.fraction.transport("odbc", t -> t.socketBinding("teiid-odbc").protocol(Transport.Protocol.TEIID));
        }
    }
}
