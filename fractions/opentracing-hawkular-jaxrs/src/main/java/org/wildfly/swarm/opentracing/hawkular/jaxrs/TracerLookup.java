package org.wildfly.swarm.opentracing.hawkular.jaxrs;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
public interface TracerLookup {

    String JNDI_NAME = "swarm/opentracing/tracer";

    static TracerLookup lookup() throws NamingException {
        InitialContext context = new InitialContext();
        return (TracerLookup) context.lookup("jboss/" + TracerLookup.JNDI_NAME);
    }

    ServerTracingDynamicFeature.Builder get();
}
