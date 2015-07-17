package org.wildfly.swarm.msc;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchiveEvent;
import org.jboss.shrinkwrap.api.ArchiveEventHandler;
import org.jboss.shrinkwrap.api.ArchiveFormat;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.jboss.shrinkwrap.impl.base.container.ContainerBase;
import org.jboss.shrinkwrap.spi.ArchiveFormatAssociable;

/**
 * @author Bob McWhirter
 */

public class ServiceActivatorArchiveImpl extends AssignableBase<ArchiveBase<?>> implements ServiceActivatorArchive {

    private final ServiceActivatorAsset asset;

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public ServiceActivatorArchiveImpl(ArchiveBase<?> archive) {
        super(archive);
        getArchive().addHandlers( new MyListener() );
        this.asset = new ServiceActivatorAsset();
        if ( getArchive().getName().endsWith( ".war" ) ) {
            getArchive().as(WebArchive.class).addAsManifestResource(this.asset, "services/" + ServiceActivator.class.getName() );
        } else if ( getArchive().getName().endsWith( ".jar" ) ) {
            getArchive().as(JavaArchive.class).addAsManifestResource(this.asset, "services/" + ServiceActivator.class.getName());
        }
    }

    public ServiceActivatorArchive addServiceActivator(Class<? extends ServiceActivator> cls) {
        if ( getArchive().getName().endsWith( ".war" ) ) {
            getArchive().as(WebArchive.class).addClass( cls );
        } else if ( getArchive().getName().endsWith( ".jar" ) ) {
            getArchive().as(JavaArchive.class).addClass( cls );
        }
        this.asset.addServiceActivator( cls );
        return this;
    }

    public static final class MyListener implements  ArchiveEventHandler {
        @Override
        public void handle(ArchiveEvent event) {
            System.err.println( "handle: " + event + ", " + event.getPath() );
        }
    }

}


