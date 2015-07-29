package org.wildfly.swarm.runtime.logging;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.wildfly.swarm.logging.handlers.ConsoleHandler;
import org.wildfly.swarm.logging.handlers.CustomHandler;
import org.wildfly.swarm.logging.handlers.FileHandler;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.logging.RootLogger;
import org.wildfly.swarm.logging.format.CustomFormatter;
import org.wildfly.swarm.logging.format.Formatter;
import org.wildfly.swarm.logging.format.PatternFormatter;
import org.wildfly.swarm.logging.handlers.Handler;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class LoggingConfiguration extends AbstractServerConfiguration<LoggingFraction> {
    private static final String DEFAULT_LEVEL = "INFO";

    private final PathAddress loggingAddress = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "logging"));

    public LoggingConfiguration() {
        super(LoggingFraction.class);
    }

    @Override
    public LoggingFraction defaultFraction() {
        return LoggingFraction.createDefaultLoggingFraction();
    }

    @Override
    public List<ModelNode> getList(LoggingFraction fraction) {
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

        ModelNode subsys = new ModelNode();
        subsys.get(OP_ADDR).set(this.loggingAddress.toModelNode());
        subsys.get(OP).set(ADD);
        list.add(subsys);

        addFormatters(fraction, list);
        addConsoleHandler(fraction, list);
        addHandlers(fraction, list);
        addRootLogger(fraction, list);

        return list;
    }

    private void addFormatters(LoggingFraction fraction, List<ModelNode> list) {
        for (Formatter each : fraction.formatters()) {
            if (each.isPattern()) {
                addPatternFormatter((PatternFormatter) each, list);
            } else if (each.isCustom()) {
                addCustomFormatter((CustomFormatter) each, list);
            }
        }
    }

    private void addPatternFormatter(PatternFormatter formatter, List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(loggingAddress.append("pattern-formatter", formatter.getName()).toModelNode());
        node.get(OP).set(ADD);
        node.get("pattern").set(formatter.getPattern());
        list.add(node);
    }

    private void addCustomFormatter(CustomFormatter formatter, List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(loggingAddress.append("custom-formatter", formatter.getName()).toModelNode());
        node.get(OP).set(ADD);
        node.get("module").set(formatter.getModule());
        node.get("class").set(formatter.getClassName());
        list.add(node);
    }

    private void addConsoleHandler(LoggingFraction fraction, List<ModelNode> list) {
        ConsoleHandler handler = fraction.consoleHandler();
        if (handler == null) {
            return;
        }
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(loggingAddress.append("console-handler", "CONSOLE").toModelNode());
        node.get(OP).set(ADD);
        node.get("level").set(handler.getLevel());
        node.get("named-formatter").set(handler.getFormatter());
        list.add(node);
    }

    private void addHandlers(LoggingFraction fraction, List<ModelNode> list) {
        for (Handler each : fraction.handlers()) {
            if (each.isFile()) {
                addFileHandler((FileHandler)each, list);
            } else if (each.isCustom()) {
                addCustomHandler((CustomHandler)each, list);
            }
        }
    }

    private void addFileHandler(FileHandler handler, List<ModelNode> list) {
        ModelNode node = new ModelNode();

        node.get(OP_ADDR).set(loggingAddress.append("file-handler", handler.name()).toModelNode());
        node.get(OP).set(ADD);
        node.get("level").set(handler.level());
        node.get("named-formatter").set(handler.formatter());

        ModelNode file = new ModelNode();
        file.get("path").set(handler.path());
        file.get("relative-to").set("jboss.server.log.dir");
        node.get("file").set(file);
        node.get("append").set(true);

        list.add(node);
    }

    private void addCustomHandler(CustomHandler handler, List<ModelNode> list) {
        ModelNode node = new ModelNode();

        node.get(OP_ADDR).set(loggingAddress.append("custom-handler", handler.name()).toModelNode());
        node.get(OP).set(ADD);
        node.get("class").set(handler.className());
        node.get("module").set(handler.module());
        node.get("named-formatter").set(handler.formatter());

        ModelNode properties = new ModelNode();
        handler.properties().forEach((key, value) -> properties.get((String) key).set(new ValueExpression((String) value)));
        node.get("properties").set(properties);

        list.add(node);
    }

    private void addRootLogger(LoggingFraction fraction, List<ModelNode> list) {
        RootLogger logger = fraction.rootLogger();
        if (logger == null) {
            return;
        }
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(loggingAddress.append("root-logger", "ROOT").toModelNode());
        node.get(OP).set(ADD);
        for (String handler : logger.getHandlers()) {
            node.get("handlers").add(handler);
        }
        node.get("level").set(logger.getLevel());
        list.add(node);
    }

}
