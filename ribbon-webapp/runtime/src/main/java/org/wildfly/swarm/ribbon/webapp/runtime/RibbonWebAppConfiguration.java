package org.wildfly.swarm.ribbon.webapp.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.netflix.ribbon.RibbonArchive;
import org.wildfly.swarm.ribbon.webapp.RibbonWebAppFraction;
import org.wildfly.swarm.undertow.WARArchive;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Lance Ball
 */
public class RibbonWebAppConfiguration extends AbstractServerConfiguration<RibbonWebAppFraction> {

    private static final String DEFAULT_CONTEXT = "/ribbon";

    public RibbonWebAppConfiguration() {
        super(RibbonWebAppFraction.class);
    }

    @Override
    public RibbonWebAppFraction defaultFraction() {
        return new RibbonWebAppFraction();
    }

    @Override
    public List<Archive> getImplicitDeployments(RibbonWebAppFraction fraction) throws Exception {
        String context = System.getProperty( "wildfly.swarm.ribbon.context.path" );
        if (context == null) context = DEFAULT_CONTEXT;

        List<Archive> list = new ArrayList<>();
        WARArchive war = ShrinkWrap.create( WARArchive.class, "ribbon-webapp.war" );
        war.addClass( RibbonToTheCurbSSEServlet.class );
        war.addModule("org.wildfly.swarm.netflix.ribbon");
        war.addAsWebResource(new ClassLoaderAsset("ribbon.js", this.getClass().getClassLoader()), "ribbon.js");
        war.setContextRoot(context);
        war.as(RibbonArchive.class);
        list.add(war);
        return list;
    }

}
