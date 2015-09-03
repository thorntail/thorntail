package org.wildfly.swarm.jolokia;

import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class JolokiaFraction implements Fraction {

    private final String context;

    public JolokiaFraction() {
        this( "jolokia" );
    }

    public JolokiaFraction(String context) {
        this.context = context;
    }

    public String context() {
        return this.context;
    }
}
