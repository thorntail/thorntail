package org.wildfly.swarm.it;

import static org.fest.assertions.Assertions.assertThat;
/**
 * @author Bob McWhirter
 */
public class LogAssert {

    private final Log log;

    LogAssert(Log log) {
        this.log = log;
    }

    public LogAssert hasLineContaining(String match) {
        assertThat( this.log.getLines().stream().filter( e->e.contains(match)).count() ).isGreaterThan( 0);
        return this;
    }

}
