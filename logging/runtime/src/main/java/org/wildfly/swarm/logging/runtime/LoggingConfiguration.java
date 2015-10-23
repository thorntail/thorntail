package org.wildfly.swarm.logging.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.logging.LoggingFraction;

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
}
