package org.wildfly.swarm.ribbon.webapp.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
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
        WARArchive war = ShrinkWrap.create( WARArchive.class );
        war.addClass( RibbonToTheCurbSSEServlet.class );
        war.addModule("org.wildfly.swarm.ribbon");
        war.addAsResource(new ClassLoaderAsset("ribbon.js"), "js/ribbon.js");
        list.add(war);
        return list;
    }

}
