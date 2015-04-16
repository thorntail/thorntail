package org.wildfly.swarm.logging;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.Subsystem;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class LoggingSubsystem implements Subsystem {

    private final ModelNode address = new ModelNode();
    private final PathAddress loggingAddress = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "logging"));
    private List<ModelNode> list = new ArrayList<>();

    public LoggingSubsystem() {

        this.address.setEmptyList();

        ModelNode add = new ModelNode();
        add.get(OP_ADDR).set(address).add(EXTENSION, "org.jboss.as.logging");
        add.get(OP).set(ADD);
        list.add(add);

        ModelNode subsys = new ModelNode();
        subsys.get(OP_ADDR).set(this.loggingAddress.toModelNode());
        subsys.get(OP).set(ADD);
        list.add(subsys);
    }

    public LoggingSubsystem formatter(String name, String pattern) {

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(loggingAddress.append("pattern-formatter", name ).toModelNode());
        node.get(OP).set(ADD);
        //node.get("pattern").set("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
        node.get("pattern").set(pattern);
        this.list.add(node);

        return this;
    }

    public LoggingSubsystem consoleHandler(String level, String formatter) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(loggingAddress.append("console-handler", "CONSOLE").toModelNode());
        node.get(OP).set(ADD);
        node.get("level").set( level );
        node.get("named-formatter").set( formatter );
        this.list.add(node);

        return this;
    }

    public LoggingSubsystem rootLogger(String handler, String level) {
        ModelNode node = new ModelNode();
        node.get( OP_ADDR ).set( loggingAddress.append( "root-logger", "ROOT" ).toModelNode() );
        node.get( OP ).set(ADD );
        node.get( "handlers" ).add( handler );
        node.get( "level" ).set( level );
        this.list.add( node );
        return this;
    }

    public List<ModelNode> getList() {
        return this.list;
    }
}
