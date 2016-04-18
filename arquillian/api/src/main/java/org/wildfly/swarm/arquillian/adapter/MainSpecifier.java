package org.wildfly.swarm.arquillian.adapter;

/**
 * @author Bob McWhirter
 */
public class MainSpecifier {

    private final String className;
    private final String[] args;

    public MainSpecifier(String className, String...args) {
        this.className = className;
        this.args = args;
    }

    public String getClassName() {
        return this.className;
    }

    public String[] getArgs() {
        return this.args;
    }
}
