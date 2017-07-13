/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.cassandra.runtime;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.cassandra.CassandraFraction;
import org.wildfly.swarm.container.util.DriverModuleBuilder;
import org.wildfly.swarm.container.util.Messages;

/**
 * Auto-detection for Cassandra NoSQL driver (based on org.wildfly.swarm.datasources.runtime.DriverInfo, thanks Bob!).
 * <p>
 * <p>mark as a {@link javax.inject.Singleton}, due
 * to the fact that state is retained as to if the driver has or has not
 * been detected.</p>
 *
 * @author Scott Marlow
 */
@ApplicationScoped
public class CassandraDriverInfo extends DriverModuleBuilder {

    public CassandraDriverInfo() {
        super("Cassandra", "com.datastax.driver.core.Cluster",
                new String[]{
                        "com.codahale.metrics.Metric",
                        "io.netty.buffer.PooledByteBufAllocator",
                        "io.netty.channel.group.ChannelGroup",
                        "io.netty.util.Timer",
                        "io.netty.handler.codec.MessageToMessageDecoder",
                        "io.netty.handler.timeout.IdleStateHandler",
                        "org.slf4j.impl.StaticLoggerBinder",
                        "org.slf4j.LoggerFactory",
                        "com.datastax.driver.core.Cluster",
                        "com.datastax.driver.core.Session",
                        "com.datastax.driver.core.Message",
                        "com.google.common.util.concurrent.AsyncFunction"
                },
                new ModuleIdentifier[]{
                        ModuleIdentifier.create("javax.api"),
                        ModuleIdentifier.create("org.picketbox"),
                        ModuleIdentifier.create("sun.jdk"),
                        ModuleIdentifier.create("org.slf4j"),
                        ModuleIdentifier.create("javax.transaction.api")
                });
    }

    public boolean detect(CassandraFraction fraction) {

        if (0 == fraction.subresources().cassandras().size()) {
            return false;  // no NoSQL profiles defined
        }

        final String moduleName = (fraction.subresources().cassandras().get(0).module() != null ?
                fraction.subresources().cassandras().get(0).module() : "com.datastax.cassandra.driver-core");

        // ensure that application only specifies one Cassandra module
        fraction.subresources().cassandras().forEach(cassandra -> {
            if (cassandra.module() != null && !moduleName.equals(cassandra.module())) {
                throw Messages.MESSAGES.cannotAddReferenceToModule(cassandra.module(), moduleName);
            }
        });

        return super.detect(moduleName);
    }
}

