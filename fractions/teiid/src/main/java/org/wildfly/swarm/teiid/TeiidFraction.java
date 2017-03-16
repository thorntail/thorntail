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
package org.wildfly.swarm.teiid;

import org.wildfly.swarm.config.Teiid;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.config.teiid.Transport;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.teiid.TeiidProperties.DEFAULT_JDBC_PORT;
import static org.wildfly.swarm.teiid.TeiidProperties.DEFAULT_ODBC_PORT;
import static org.wildfly.swarm.teiid.TeiidProperties.PREPARED_INFINISPAN_CACHE_CONTAINER_NAME;
import static org.wildfly.swarm.teiid.TeiidProperties.RESULTSET_INFINISPAN_CACHE_CONTAINER_NAME;
import static org.wildfly.swarm.teiid.TeiidProperties.PREPARED_INFINISPAN_CACHE_NAME;
import static org.wildfly.swarm.teiid.TeiidProperties.RESULTSET_INFINISPAN_CACHE_NAME;
import static org.wildfly.swarm.teiid.TeiidProperties.JDBC_TRANSPORT_NAME;
import static org.wildfly.swarm.teiid.TeiidProperties.JDBC_SOCKET_BINDING_NAME;
import static org.wildfly.swarm.teiid.TeiidProperties.ODBC_TRANSPORT_NAME;
import static org.wildfly.swarm.teiid.TeiidProperties.ODBC_SOCKET_BINDING_NAME;

@MarshalDMR
@WildFlyExtension(module = "org.jboss.teiid", classname = "org.teiid.jboss.TeiidExtension")
public class TeiidFraction extends Teiid<TeiidFraction> implements Fraction<TeiidFraction> {

    private static final long serialVersionUID = -6070901334377803127L;

    @Configurable("swarm.teiid.jdbc.port")
    @AttributeDocumentation("Set the port for the default JDBC socket listener")
    private Defaultable<Integer> jdbcPort = integer(DEFAULT_JDBC_PORT);

    @Configurable("swarm.teiid.odbc.port")
    @AttributeDocumentation("Set the port for the default ODBC socket listener")
    private Defaultable<Integer> odbcPort = integer(DEFAULT_ODBC_PORT);

    public TeiidFraction() {

    }

    @Override
    public TeiidFraction applyDefaults() {
        return jdbcTransport();
    }

    public TeiidFraction jdbcTransport() {
        return transport(JDBC_TRANSPORT_NAME, t -> t.socketBinding(JDBC_SOCKET_BINDING_NAME).protocol(Transport.Protocol.TEIID));
    }

    public TeiidFraction odbcTransport() {
        return transport(ODBC_TRANSPORT_NAME, t -> t.socketBinding(ODBC_SOCKET_BINDING_NAME).protocol(Transport.Protocol.TEIID));
    }

    public TeiidFraction jdbcPort(int jdbcPort) {
        this.jdbcPort.set(jdbcPort);
        return this;
    }

    public int jdbcPort() {
        return this.jdbcPort.get();
    }

    public TeiidFraction odbcPort(int jdbcPort) {
        this.odbcPort.set(jdbcPort);
        return this;
    }

    public int odbcPort() {
        return this.odbcPort.get();
    }

    @Override
    public String preparedplanCacheInfinispanContainer() {
        if (super.preparedplanCacheInfinispanContainer() == null) {
            super.preparedplanCacheInfinispanContainer(PREPARED_INFINISPAN_CACHE_CONTAINER_NAME);
        }
        return super.preparedplanCacheInfinispanContainer();
    }

    @Override
    public String preparedplanCacheName() {
        if (super.preparedplanCacheName() == null) {
            super.preparedplanCacheName(PREPARED_INFINISPAN_CACHE_NAME);
        }
        return super.preparedplanCacheName();
    }

    @Override
    public String resultsetCacheInfinispanContainer() {
        if (super.resultsetCacheInfinispanContainer() == null) {
            super.resultsetCacheInfinispanContainer(RESULTSET_INFINISPAN_CACHE_CONTAINER_NAME);
        }
        return super.resultsetCacheInfinispanContainer();
    }

    @Override
    public String resultsetCacheName() {
        if (super.resultsetCacheName() == null) {
            super.resultsetCacheName(RESULTSET_INFINISPAN_CACHE_NAME);
        }
        return super.resultsetCacheName();
    }

}

