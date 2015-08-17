package org.wildfly.swarm.logging.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.logging.subsystem.asyncHandler.AsyncHandler;
import org.wildfly.swarm.config.logging.subsystem.consoleHandler.ConsoleHandler;
import org.wildfly.swarm.config.logging.subsystem.customFormatter.CustomFormatter;
import org.wildfly.swarm.config.logging.subsystem.customHandler.CustomHandler;
import org.wildfly.swarm.config.logging.subsystem.fileHandler.FileHandler;
import org.wildfly.swarm.config.logging.subsystem.patternFormatter.PatternFormatter;
import org.wildfly.swarm.config.logging.subsystem.rootLogger.Root;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.logging.LoggingFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class LoggingConfiguration extends AbstractServerConfiguration<LoggingFraction> {
    private static final String DEFAULT_LEVEL = "INFO";

    private final PathAddress loggingAddress = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "logging"));

    public LoggingConfiguration() {
        super(LoggingFraction.class);
    }

    @Override
    public LoggingFraction defaultFraction() {
        String prop = System.getProperty("swarm.logging");
        if ( prop != null ) {
            prop = prop.trim().toLowerCase();

            if ( prop.equals("debug" ) ) {
                return LoggingFraction.createDebugLoggingFraction();
            } else if (prop.equals("trace" ) ) {
                return LoggingFraction.createTraceLoggingFraction();
            }
        }

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
        for (PatternFormatter formatter : fraction.patternFormatters()) {
            addPatternFormatter(formatter, list);
        }
        for (CustomFormatter formatter : fraction.customFormatters()) {
            addCustomFormatter(formatter, list);
        }
    }

    private void addPatternFormatter(PatternFormatter formatter, List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(loggingAddress.append("pattern-formatter", formatter.getKey()).toModelNode());
        node.get(OP).set(ADD);
        node.get("pattern").set(formatter.pattern());
        list.add(node);
    }

    private void addCustomFormatter(CustomFormatter formatter, List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(loggingAddress.append("custom-formatter", formatter.getKey()).toModelNode());
        node.get(OP).set(ADD);
        node.get("module").set(formatter.module());
        node.get("class").set(formatter.attributeClass());

        // TODO: CustomFormatter needs to handle properties
//        if (formatter.properties().size() > 0) {
//            ModelNode properties = new ModelNode();
//            StringBuffer buff = new StringBuffer();
//            boolean first = true;
//            for (Map.Entry<Object, Object> entry : formatter.properties().entrySet()) {
//                if (!first) {
//                    buff.append(",");
//                }
//                buff.append(entry.getKey());
//                buff.append("=");
//                buff.append(entry.getValue());
//                first = false;
//            }
//            properties.get("metaData").set(new ValueExpression(buff.toString()));
//            node.get("properties").set(properties);
//        }

        list.add(node);
    }

    private void addConsoleHandler(LoggingFraction fraction, List<ModelNode> list) {
        for (ConsoleHandler handler : fraction.consoleHandlers()) {
            ModelNode node = new ModelNode();
            node.get(OP_ADDR).set(loggingAddress.append("console-handler", "CONSOLE").toModelNode());
            node.get(OP).set(ADD);
            node.get("level").set(handler.level());
            node.get("named-formatter").set(handler.formatter());
            list.add(node);
        }
    }

    private void addHandlers(LoggingFraction fraction, List<ModelNode> list) {
        for (AsyncHandler handler : fraction.asyncHandlers()) {
            // TODO: ADD ASYNC
        }
        for (ConsoleHandler handler : fraction.consoleHandlers()) {
            // TODO: ADD CONSOLE
        }
        for (CustomHandler handler : fraction.customHandlers()) {
            addCustomHandler(handler, list);
        }
        for (FileHandler handler : fraction.fileHandlers()) {
            addFileHandler(handler, list);
        }
        // TODO: Add methods for PeriodicRotatingFileHandler, PeriodicSizeRotatingFileHandler, SizeRotatingFileHandler
    }

    private void addFileHandler(FileHandler handler, List<ModelNode> list) {
        ModelNode node = new ModelNode();

        node.get(OP_ADDR).set(loggingAddress.append("file-handler", handler.name()).toModelNode());
        node.get(OP).set(ADD);
        node.get("level").set(handler.level());
        node.get("named-formatter").set(handler.formatter());

        ModelNode file = new ModelNode();
        // TODO: FileHandler needs to handle paths
        //file.get("path").set(handler.path());
        file.get("relative-to").set("jboss.server.log.dir");
        node.get("file").set(file);
        node.get("append").set(true);

        list.add(node);
    }

    private void addCustomHandler(CustomHandler handler, List<ModelNode> list) {
        ModelNode node = new ModelNode();

        node.get(OP_ADDR).set(loggingAddress.append("custom-handler", handler.name()).toModelNode());
        node.get(OP).set(ADD);
        node.get("class").set(handler.attributeClass());
        node.get("module").set(handler.module());
        node.get("named-formatter").set(handler.formatter());

        // TODO: CustomHandlers should have properties
//        if (handler.properties().size() > 0) {
//            ModelNode properties = new ModelNode();
//            handler.properties().forEach((key, value) -> properties.get((String) key).set(new ValueExpression((String) value)));
//            node.get("properties").set(properties);
//        }

        list.add(node);
    }

    private void addRootLogger(LoggingFraction fraction, List<ModelNode> list) {
        Root logger = fraction.rootLogger();
        if (logger == null) {
            return;
        }
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(loggingAddress.append("root-logger", "ROOT").toModelNode());
        node.get(OP).set(ADD);
        // TODO: Root needs to have a list of handlers?
//        for (String handler : logger.getHandlers()) {
//            node.get("handlers").add(handler);
//        }
        node.get("level").set(logger.level());
        list.add(node);
    }

}
