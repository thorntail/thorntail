package org.jboss.unimbus.testsuite.proof;

import java.net.InetSocketAddress;
import java.net.URL;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.condition.ConditionExtension;
import org.jboss.unimbus.servlet.Primary;

/**
 * Created by bob on 1/15/18.
 */
@Path("/")
public class ProofResource {

    @GET
    @Path("/")
    public String get() {
        return "Hello! " + this.url + " // " + this.entityManager;
    }

    @Inject
    @Primary
    URL url;

    @Inject
    @PersistenceContext
    EntityManager entityManager;

    @Inject
    @ConfigProperty(name="web.primary.port")
    int port;
}
