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
package org.wildfly.swarm.logging.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.bootstrap.logging.InitialLoggerManager;
import org.wildfly.swarm.bootstrap.logging.LevelNode;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.logging.LoggingProperties;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;
import org.wildfly.swarm.spi.runtime.MarshallingServerConfiguration;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class LoggingConfiguration extends MarshallingServerConfiguration<LoggingFraction> {

    public static final String EXTENSION_MODULE = "org.jboss.as.logging";

    public LoggingConfiguration() {
        super(LoggingFraction.class, EXTENSION_MODULE);
    }

    @Override
    public LoggingFraction defaultFraction() {
        String prop = System.getProperty(LoggingProperties.LOGGING);
        if (prop != null) {
            prop = prop.trim().toUpperCase();

            Level level;
            try {
                level = Level.valueOf(prop);
            } catch (IllegalArgumentException e) {
                return LoggingFraction.createDefaultLoggingFraction();
            }

            return LoggingFraction.createDefaultLoggingFraction(level);
        }

        return LoggingFraction.createDefaultLoggingFraction();
    }

    @Override
    public List<ModelNode> getList(LoggingFraction fraction) throws Exception {
        if (fraction == null) {
            fraction = defaultFraction();
        }

        LevelNode root = InitialLoggerManager.INSTANCE.getRoot();

        apply(root, fraction);

        return super.getList(fraction);
    }

    @Override
    public Optional<Map<QName, XMLElementReader<List<ModelNode>>>> getSubsystemParsers() throws Exception {
        return AbstractParserFactory.mapParserNamespaces(new LoggingParserFactory());
    }

    private void apply(LevelNode node, LoggingFraction fraction) {
        if (!node.getName().equals("")) {
            fraction.logger(node.getName(), (l) -> {
                l.level(Level.valueOf(node.getLevel().toString()));
            });
        }
        for (LevelNode each : node.getChildren()) {
            apply(each, fraction);
        }
    }
}
