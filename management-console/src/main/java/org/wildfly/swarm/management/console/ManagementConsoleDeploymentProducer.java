package org.wildfly.swarm.management.console;

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
public class ManagementConsoleDeploymentProducer {

    @Inject
    private ArtifactLookup lookup;

    @Inject @Any
    private ManagementConsoleFraction fraction;

    @Inject
    @ConfigurationValue(ManagementConsoleProperties.CONTEXT)
    private String context;

    @Produces
    public Archive managementConsoleWar() throws Exception {
        if ( this.context == null ) {
            this.context = fraction.contextRoot();
        }
        return this.lookup.artifact("org.jboss.as:jboss-as-console:jar:2.8.25.Final:resources", "management-console-ui.war")
                .as(WARArchive.class)
                .setContextRoot(this.context);
    }

}
