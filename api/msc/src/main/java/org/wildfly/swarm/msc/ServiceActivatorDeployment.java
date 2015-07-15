package org.wildfly.swarm.msc;

import java.util.ArrayList;
import java.util.List;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Deployment;

/**
 * @author Bob McWhirter
 */
public class ServiceActivatorDeployment implements Deployment {

    private final JavaArchive archive;

    private final List<Class<? extends ServiceActivator>> activators = new ArrayList<>();

    public ServiceActivatorDeployment(Container container) {
        this.archive = container.create("services.jar", JavaArchive.class);
    }

    public void addServiceActivator(Class<? extends ServiceActivator> activator) {
        this.archive.addClass(activator);
        this.activators.add(activator);
    }

    public void addClass(Class cls) {
        this.archive.addClass(cls);
    }

    @Override
    public JavaArchive getArchive() {
        return getArchive(false);
    }

    public JavaArchive getArchive(boolean finalize) {
        if (finalize) {
            this.archive.addAsServiceProvider(ServiceActivator.class, (Class[]) this.activators.toArray(new Class[]{}));
            addJBossDeploymentStructure();
        }
        return this.archive;
    }

    private void addJBossDeploymentStructure() {
        StringAsset structureXml = new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  \n" +
                "<jboss-deployment-structure>  \n" +
                "    <deployment>  \n" +
                "         <dependencies>  \n" +
                "              <module name=\"APP\" slot=\"dependencies\"/>  \n" +
                "              <module name=\"org.wildfly.clustering.api\" optional=\"true\"/> \n" +
                "        </dependencies>  \n" +
                "    </deployment>  \n" +
                "</jboss-deployment-structure>\n");

        this.archive.addAsManifestResource(structureXml, "jboss-deployment-structure.xml");
    }
}
