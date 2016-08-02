package org.wildfly.swarm.jolokia;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.annotations.ConfigurationValue;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 */
@Singleton
public class JolokiaWarDeploymentProducer {

    @Inject
    @Any
    private JolokiaFraction fraction;

    @Inject
    private ArtifactLookup lookup;

    @Inject
    @ConfigurationValue( "swarm.jolokia.context")
    private String context;

    @Produces
    public Archive jolokiaWar() throws Exception {

        if ( this.context == null ) {
            this.context = this.fraction.context();
        }

        Archive deployment = this.lookup.artifact("org.jolokia:jolokia-war:war:*", "jolokia.war");

        deployment.as( WARArchive.class).setContextRoot( this.context );

        return deployment;
    }
}
