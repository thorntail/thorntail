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
        super(container.getShrinkWrapDomain().getArchiveFactory().create(WebArchive.class));
        setup();
    }

    protected void setup() {
        boolean result = setupUsingAppPath() || setupUsingAppArtifact() || setupUsingMaven();
    }

    protected boolean setupUsingAppPath() {
        if (System.getProperty("wildfly.swarm.app.path") != null) {
            addJavaClassPathToWebInfLib();
            return true;
        }
        return false;
    }

    protected boolean setupUsingAppArtifact() {
        return false;
    }

    protected boolean setupUsingMaven() {
        addJavaClassPathToWebInfLib();
        return true;
    }


    public void setApplication(Class<? extends Application> application) {
        this.hasApplication = true;
        this.archive.addClass(application);
    }

    public void addResource(Class<?> resourceClass) {
        this.archive.addClass(resourceClass);
    }

    protected void ensureApplication() {
        if (!this.hasApplication) {
            setApplication(DefaultApplication.class);
        }
    }

    @Override
    public WebArchive getArchive() {
        ensureApplication();
        return super.getArchive();
    }


}
