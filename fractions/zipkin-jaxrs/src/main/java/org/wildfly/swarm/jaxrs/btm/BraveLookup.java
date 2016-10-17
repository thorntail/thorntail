package org.wildfly.swarm.jaxrs.btm;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.github.kristofa.brave.Brave;

/**
 * @author Heiko Braun
 * @since 17/10/16
 */
public interface BraveLookup {

    String JNDI_NAME = "swarm/zipkin/brave";

    static BraveLookup lookup() throws NamingException {
        InitialContext context = new InitialContext();
        return (BraveLookup) context.lookup("jboss/" + BraveLookup.JNDI_NAME);
    }


    Brave get();
}
