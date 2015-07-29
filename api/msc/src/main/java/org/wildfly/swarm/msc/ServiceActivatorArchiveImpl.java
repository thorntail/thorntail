package org.wildfly.swarm.msc;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;

/**
 * @author Bob McWhirter
 */

public class ServiceActivatorArchiveImpl extends AssignableBase<ArchiveBase<?>> implements ServiceActivatorArchive {

    private ServiceActivatorAsset asset;

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public ServiceActivatorArchiveImpl(ArchiveBase<?> archive) {
        super(archive);

        if ( getArchive().getName().endsWith( ".war" ) ) {
            Node node = getArchive().get("WEB-INF/classes/META-INF/services/" + ServiceActivator.class.getName());
            if ( node != null ) {
                this.asset = (ServiceActivatorAsset) node.getAsset();
            } else {
                this.asset = new ServiceActivatorAsset();
                getArchive().add( this.asset, "WEB-INF/classes/META-INF/services/" + ServiceActivator.class.getName() );
            }
        }  else if ( getArchive().getName().endsWith( ".jar" ) ) {
            Node node = getArchive().get("META-INF/services/" + ServiceActivator.class.getName());
            if ( node != null ) {
                this.asset = (ServiceActivatorAsset) node.getAsset();
            } else {
                this.asset = new ServiceActivatorAsset();
                getArchive().add( this.asset, "META-INF/services/" + ServiceActivator.class.getName() );
            }
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

    public ServiceActivatorArchive addServiceActivator(String className) {
        this.asset.addServiceActivator( className );
        return this;
    }

}


