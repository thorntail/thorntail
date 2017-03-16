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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.container.runtime.config.DefaultSocketBindingGroupProducer;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.teiid.TeiidFraction;

import static org.wildfly.swarm.teiid.TeiidProperties.JDBC_SOCKET_BINDING_NAME;
import static org.wildfly.swarm.teiid.TeiidProperties.ODBC_SOCKET_BINDING_NAME;

@Pre
@ApplicationScoped
public class TeiidSocketBindingCustomizer implements Customizer {

    @Inject
    @Named(DefaultSocketBindingGroupProducer.STANDARD_SOCKETS)
    private SocketBindingGroup group;

    @Inject
    private TeiidFraction teiid;

    @Override
    public void customize() {
        this.group.socketBinding(new SocketBinding(JDBC_SOCKET_BINDING_NAME).port(this.teiid.jdbcPort()));
        this.group.socketBinding(new SocketBinding(ODBC_SOCKET_BINDING_NAME).port(this.teiid.odbcPort()));
        this.teiid.jdbcTransport();
    }


}
