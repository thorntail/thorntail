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
package org.wildfly.swarm.container.runtime;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.inject.Vetoed;

import org.jboss.as.logging.logmanager.ConfigurationPersistence;
import org.jboss.logmanager.Configurator;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.PropertyConfigurator;
import org.jboss.logmanager.config.LogContextConfiguration;
import org.wildfly.swarm.bootstrap.logging.InitialLoggerManager;
import org.wildfly.swarm.bootstrap.logging.LevelNode;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class LoggingConfigurator extends ConfigurationPersistence implements Configurator {

    /**
     * Construct an instance.
     */
    public LoggingConfigurator() {
        this(LogContext.getSystemLogContext());
    }

    /**
     * Construct a new instance.
     *
     * @param context the log context to be configured
     */
    public LoggingConfigurator(LogContext context) {
        this.context = context;
        this.propertyConfigurator = new PropertyConfigurator(this.context);
    }

    @Override
    public void configure(InputStream inputStream) throws IOException {
        this.propertyConfigurator.configure(inputStream);
        LogContextConfiguration config = this.propertyConfigurator.getLogContextConfiguration();
        config.getHandlerConfiguration("CONSOLE").setLevel("ALL");
        LevelNode root = InitialLoggerManager.INSTANCE.getRoot();
        apply(root, config);
        config.commit();
    }

    protected void apply(LevelNode node, LogContextConfiguration config) {
        if (!node.getName().equals("")) {
            config.addLoggerConfiguration(node.getName()).setLevel(node.getLevel().toString());
        }

        for (LevelNode each : node.getChildren()) {
            apply(each, config);
        }
    }

    private final LogContext context;

    private final PropertyConfigurator propertyConfigurator;
}
