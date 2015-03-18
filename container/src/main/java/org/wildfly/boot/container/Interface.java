package org.wildfly.boot.container;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INET_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public class Interface {

    private final String name;
    private final String expression;

    public Interface(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    public String getName() {
        return this.name;
    }

    public String getExpression() {
        return this.expression;
    }

    public ModelNode getNode() {
        ModelNode node = new ModelNode();

        node.get(OP).set(ADD);
        node.get(OP_ADDR).set("interface", name);
        node.get(INET_ADDRESS).set(new ValueExpression(expression));

        return node;
    }
}
