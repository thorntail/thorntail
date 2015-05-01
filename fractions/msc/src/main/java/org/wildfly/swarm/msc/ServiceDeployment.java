package org.wildfly.swarm.msc;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ClassAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.vfs.VirtualFile;
import org.wildfly.swarm.container.Deployment;
import org.wildfly.swarm.shrinkwrap.ShrinkWrapDeployment;

import java.io.IOException;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class ServiceDeployment implements Deployment {

    private static int COUNTER = 0;
    private final ShrinkWrapDeployment<JavaArchive> delegate;

    private boolean instanceServiceProviderAdded = false;

    public ServiceDeployment() {
        this.delegate = new ShrinkWrapDeployment( archiveName(), JavaArchive.class);
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    public ServiceDeployment addService(Service service) {
        ServiceDeploymentRegistry registry = ServiceDeploymentRegistry.get(getName());
        registry.addService(service);
        if ( ! this.instanceServiceProviderAdded ) {
            this.delegate.getArchive().addAsServiceProvider(ServiceActivator.class, ServiceInstanceActivator.class);
            this.instanceServiceProviderAdded = true;
        }

        return this;
    }

    @Override
    public VirtualFile getContent() throws IOException {
        addJBossDeploymentStructure();

        return this.delegate.getContent();
    }

    private void addJBossDeploymentStructure() {
        StringAsset structureXml = new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  \n" +
                "<jboss-deployment-structure>  \n" +
                "    <deployment>  \n" +
                "         <dependencies>  \n" +
                "              <module name=\"APP\" slot=\"dependencies\"/>  \n" +
                "              <module name=\"org.wildfly.swarm.msc\"/>  \n" +
                "        </dependencies>  \n" +
                "    </deployment>  \n" +
                "</jboss-deployment-structure>\n");

        this.delegate.getArchive().addAsManifestResource(structureXml, "jboss-deployment-structure.xml");
    }

    private static String archiveName() {
        return System.getProperty( "wildfly.swarm.app.name" ).replace( ".jar", "-service-" ) + ( ++COUNTER ) + ".jar";
    }
}
