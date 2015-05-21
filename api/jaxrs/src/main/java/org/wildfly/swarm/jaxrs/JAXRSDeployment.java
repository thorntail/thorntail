package org.wildfly.swarm.jaxrs;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.WarDeployment;

import javax.ws.rs.core.Application;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class JAXRSDeployment extends WarDeployment {

    private boolean hasApplication;

    public JAXRSDeployment(Container container) throws IOException, ModuleLoadException {
        super( container.getShrinkWrapDomain().getArchiveFactory().create( WebArchive.class ) );
    }

    public void setApplication(Class<? extends Application> application) {
        this.hasApplication = true;
        this.archive.addClass(application);
    }

    public void addResource(Class<?> resourceClass) {
        this.archive.addClass(resourceClass);
    }

    protected void ensureApplication() {
        if ( ! this.hasApplication ) {
            setApplication( DefaultApplication.class );
        }
    }

    @Override
    public WebArchive getArchive() {
        ensureApplication();
        return super.getArchive();
    }


}
