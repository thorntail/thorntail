package org.wildfly.swarm.container;

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
}
