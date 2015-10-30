package org.wildfly.swarm.ribbon.webapp.runtime;

import com.netflix.ribbon.Ribbon;
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
        war.addModule("org.wildfly.swarm.netflix.ribbon");
        war.addAsResource(new ClassLoaderAsset("ribbon.js", this.getClass().getClassLoader()), "js/ribbon.js");
        war.as(RibbonArchive.class);
        list.add(war);
        return list;
    }

}
