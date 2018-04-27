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
package org.wildfly.swarm.jca;

import java.util.HashMap;
import java.util.Map;

import org.wildfly.swarm.config.JCA;
import org.wildfly.swarm.config.jca.ArchiveValidation;
import org.wildfly.swarm.config.jca.BeanValidation;
import org.wildfly.swarm.config.jca.BootstrapContext;
import org.wildfly.swarm.config.jca.CachedConnectionManager;
import org.wildfly.swarm.config.jca.LongRunningThreads;
import org.wildfly.swarm.config.jca.ShortRunningThreads;
import org.wildfly.swarm.config.jca.Workmanager;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 */
@WildFlyExtension(module = "org.jboss.as.connector", classname = "org.jboss.as.connector.subsystems.jca.JcaExtension")
@MarshalDMR
public class JCAFraction extends JCA<JCAFraction> implements Fraction<JCAFraction> {

    private static final String DEFAULT = "default";

    public static JCAFraction createDefaultFraction() {
        return new JCAFraction().applyDefaults();
    }

    public JCAFraction applyDefaults() {
        Map<Object, Object> keepAlive = new HashMap<>();
        keepAlive.put("time", 10L);
        keepAlive.put("unit", "SECONDS");

        archiveValidation(new ArchiveValidation()
                                  .enabled(true)
                                  .failOnError(true)
                                  .failOnWarn(false))
                .beanValidation(new BeanValidation()
                                        .enabled(true))
                .workmanager(new Workmanager(DEFAULT)
                                     .name(DEFAULT)
                                     .shortRunningThreads(new ShortRunningThreads(DEFAULT)
                                                                  .coreThreads(50)
                                                                  .queueLength(50)
                                                                  .maxThreads(50)
                                                                  .keepaliveTime(keepAlive))
                                     .longRunningThreads(new LongRunningThreads(DEFAULT)
                                                                 .coreThreads(50)
                                                                 .queueLength(50)
                                                                 .maxThreads(50)
                                                                 .keepaliveTime(keepAlive)))
                .bootstrapContext(new BootstrapContext(DEFAULT)
                                          .workmanager(DEFAULT)
                                          .name(DEFAULT))
                .cachedConnectionManager(new CachedConnectionManager().install(true));

        return this;
    }
}
