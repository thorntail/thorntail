package org.wildfly.swarm.ribbon.webapp.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.ribbon.webapp.RibbonWebAppFraction;
import org.wildfly.swarm.undertow.WARArchive;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Lance Ball
 */
public class RibbonWebAppConfiguration extends AbstractServerConfiguration<RibbonWebAppFraction> {

    public RibbonWebAppConfiguration() {
        super(RibbonWebAppFraction.class);
    }

    @Override
    public RibbonWebAppFraction defaultFraction() {
        return new RibbonWebAppFraction();
    }

    @Override
    public List<Archive> getImplicitDeployments(RibbonWebAppFraction fraction) throws Exception {
        List<Archive> list = new ArrayList<>();
        JavaArchive war;
        war = Swarm.artifact("org.wildfly.swarm:wildfly-swarm-ribbon-webapp-servlet:war:1.0.0.Alpha5-SNAPSHOT");
        war.as(WARArchive.class).setContextRoot("ribbon");
        list.add(war);

        return list;
    }

}
