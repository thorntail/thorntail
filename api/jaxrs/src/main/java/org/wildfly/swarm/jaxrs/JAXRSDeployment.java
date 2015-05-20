package org.wildfly.swarm.jaxrs;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.MemoryMapArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.DefaultWarDeployment;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class JAXRSDeployment extends DefaultWarDeployment {

    public JAXRSDeployment(Container container) throws IOException, ModuleLoadException {
        //super( new WebArchiveImpl( new MemoryMapArchiveImpl( "app.war", ShrinkWrap.getDefaultDomain().getConfiguration()) ) );
        //super( container.create( "app.war", WebArchive.class));
        super( container.getShrinkWrapDomain().getArchiveFactory().create( WebArchive.class ) );
    }

    public void addResource(Class<?> resourceClass) {
        getArchive().addClass(resourceClass);
    }

}
