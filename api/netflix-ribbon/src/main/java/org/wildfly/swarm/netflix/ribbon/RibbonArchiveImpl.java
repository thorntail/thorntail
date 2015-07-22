package org.wildfly.swarm.netflix.ribbon;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.msc.ServiceActivatorArchive;

import javax.xml.bind.util.JAXBSource;

/**
 * @author Bob McWhirter
 */
public class RibbonArchiveImpl extends AssignableBase<ArchiveBase<?>> implements RibbonArchive {

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public RibbonArchiveImpl(ArchiveBase<?> archive) {
        super(archive);
        as(ServiceActivatorArchive.class ).addServiceActivator( "org.wildfly.swarm.runtime.netflix.ribbon.ClusterManagerActivator" );
        as(JARArchive.class).addModule( "org.wildfly.swarm.netflix.ribbon", "runtime" );
        as(JARArchive.class).addModule( "org.wildfly.clustering.api" );
        as(JARArchive.class).addModule( "com.netflix.ribbon" );
        as(JARArchive.class).addModule( "com.netflix.archaius" );
        as(JARArchive.class).add( new RibbonConfigAsset() );
    }

    @Override
    public void setApplicationName(String name) {
        as(ServiceActivatorArchive.class ).addServiceActivator( "org.wildfly.swarm.runtime.netflix.ribbon.ApplicationAdvertiserActivator" );
        as(JARArchive.class).add(new StringAsset(name), "META-INF/netflix-ribbon-application.conf");
    }


}
