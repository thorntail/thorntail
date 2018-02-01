package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.resource.spi.ConnectionRequestInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.jca.adapters.jdbc.WrappedConnectionRequestInfo;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
//    @Vetoed
public class ConnectionRequestInfoProducer {

    @Produces
    ConnectionRequestInfo connectionRequestInfo() {
        System.err.println( "produce: " + this.username + " // " + this.password);
        return new WrappedConnectionRequestInfo(this.username, this.password);
    }

    @Inject
    @ConfigProperty(name = "datasource.username")
    String username;

    @Inject
    @ConfigProperty(name = "datasource.password")
    String password;

}
