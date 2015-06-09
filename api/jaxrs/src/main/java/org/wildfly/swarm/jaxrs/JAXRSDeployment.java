package org.wildfly.swarm.jaxrs;

import java.io.IOException;

import javax.ws.rs.core.Application;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.WarDeployment;

/**
 * @author Bob McWhirter
 */
public class JAXRSDeployment extends WarDeployment {

    private boolean hasApplication;

    public JAXRSDeployment(Container container) throws IOException, ModuleLoadException {
        this( container, null );
    }

    public JAXRSDeployment(Container container, String contextPath) throws IOException, ModuleLoadException {
        super(container, contextPath);
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
            String name = "org.wildfly.swarm.generated.WildFlySwarmDefaultJAXRSApplication";
            this.archive.add( new ByteArrayAsset( ApplicationFactory.create( name, this.contextPath )), "WEB-INF/classes/" + name.replace('.', '/' ) + ".class");
            this.hasApplication = true;
        }
    }

    @Override
    public WebArchive getArchive() {
        return getArchive(false);
    }

    public WebArchive getArchive(boolean finalize) {
        if ( finalize ) {
            ensureApplication();
        }
        return super.getArchive(finalize);
    }



}
