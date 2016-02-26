package org.wildfly.swarm.monitor.runtime;

import org.jboss.dmr.ModelNode;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The main API exposed by the monitoring service
 *
 * @author Heiko Braun
 * @since 19/02/16
 */
public interface Monitor {

    String JNDI_NAME = "swarm/monitor";

    static Monitor lookup() throws NamingException {
        InitialContext context = new InitialContext();
        return (Monitor) context.lookup("jboss/" + Monitor.JNDI_NAME);
    }

    ModelNode getNodeInfo();
    ModelNode heap();
    ModelNode threads();
}
