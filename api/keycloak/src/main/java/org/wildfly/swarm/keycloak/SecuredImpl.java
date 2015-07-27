package org.wildfly.swarm.keycloak;

import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.container.JARArchive;

/**
 * @author Bob McWhirter
 */
public class SecuredImpl extends AssignableBase<ArchiveBase<?>> implements Secured {

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public SecuredImpl(ArchiveBase<?> archive) {
        super(archive);
        getArchive().as(JARArchive.class).addModule( "org.wildfly.swarm.keycloak", "runtime" );
        getArchive().as(JARArchive.class).add( new SecuredWebXmlAsset(), "WEB-INF/web.xml" );
        getArchive().as(JARArchive.class).addAsServiceProvider("io.undertow.servlet.ServletExtension", "org.wildfly.swarm.runtime.keycloak.SecurityContextServletExtension" );
    }
}
