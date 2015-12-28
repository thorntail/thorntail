/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.logging.InitialBackingLogger;
import org.wildfly.swarm.bootstrap.logging.InitialLoggerManager;
import org.wildfly.swarm.bootstrap.logging.LevelNode;
import org.wildfly.swarm.config.Logging;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.config.logging.Level;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class LoggingConfiguration extends AbstractServerConfiguration<LoggingFraction> {

    public LoggingConfiguration() {
        super(LoggingFraction.class);
    }

    @Override
    public LoggingFraction defaultFraction() {
        String prop = System.getProperty("swarm.logging");
        if (prop != null) {
            prop = prop.trim().toLowerCase();

            if (prop.equals("debug")) {
                return LoggingFraction.createDebugLoggingFraction();
            } else if (prop.equals("trace")) {
                return LoggingFraction.createTraceLoggingFraction();
            }
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

        List<ModelNode> list = new ArrayList<>();

        ModelNode address = new ModelNode();

        address.setEmptyList();

        ModelNode add = new ModelNode();
        add.get(OP_ADDR).set(address).add(EXTENSION, "org.jboss.as.logging");
        add.get(OP).set(ADD);
        list.add(add);

        list.addAll(Marshaller.marshal(fraction));

        return list;
    }

    private void apply(LevelNode node, LoggingFraction fraction) {
        if (!node.getName().equals("")) {
            fraction.logger(node.getName(), (l) -> {
                l.level(Level.valueOf(node.getLevel().toString()));
            });
        }
        for (LevelNode each : node.getChildren()) {
            apply( each, fraction );
        }
    }
}
