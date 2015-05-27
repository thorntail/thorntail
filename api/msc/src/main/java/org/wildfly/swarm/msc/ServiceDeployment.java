package org.wildfly.swarm.msc;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Deployment;

/**
 * @author Bob McWhirter
 */
public class ServiceDeployment implements Deployment {

    private static int COUNTER = 0;
    private final JavaArchive archive;
    private boolean instanceServiceProviderAdded = false;

    public ServiceDeployment(Container container) {
        this.archive = container.create( "msc-" + (++COUNTER) + ".jar", JavaArchive.class );
    }

    public ServiceDeployment addService(Service service) {
        ServiceDeploymentRegistry registry = ServiceDeploymentRegistry.get(this.archive.getName() );
        registry.addService(service);
        if ( ! this.instanceServiceProviderAdded ) {
            this.archive.addAsServiceProvider(ServiceActivator.class.getName(), "org.wildfly.swarm.runtime.msc.ServiceInstanceActivator" );
            this.instanceServiceProviderAdded = true;
        }

        return this;
    }

    private void addJBossDeploymentStructure() {
        StringAsset structureXml = new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  \n" +
                "<jboss-deployment-structure>  \n" +
                "    <deployment>  \n" +
                "         <dependencies>  \n" +
                "              <module name=\"APP\" slot=\"dependencies\"/>  \n" +
                "              <module name=\"org.wildfly.swarm.msc\"/>  \n" +
                "              <module name=\"org.wildfly.swarm.runtime.msc\"/>  \n" +
                "        </dependencies>  \n" +
                "    </deployment>  \n" +
                "</jboss-deployment-structure>\n");

        this.archive.addAsManifestResource(structureXml, "jboss-deployment-structure.xml");
    }

    @Override
    public JavaArchive getArchive() {
        addJBossDeploymentStructure();
        return this.archive;
    }
}
