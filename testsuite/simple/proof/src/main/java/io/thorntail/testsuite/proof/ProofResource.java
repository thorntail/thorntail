package io.thorntail.testsuite.proof;

import java.net.URL;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import io.thorntail.servlet.annotation.Primary;

/**
 * Created by bob on 1/15/18.
 */
@Path("/")
public class ProofResource {

    @GET
    @Path("/")
    @Counted(monotonic = true, tags = "app=proof")
    public String get() {
        return "Hello there! " + this.url + " // " + this.entityManager;
    }

    @Inject
    @Primary
    URL url;

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    @ConfigProperty(name="web.primary.port")
    int port;

    @Inject
    ExecutorService executorService;
}
