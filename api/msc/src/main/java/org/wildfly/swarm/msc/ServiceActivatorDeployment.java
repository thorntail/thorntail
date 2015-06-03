package org.wildfly.swarm.msc;

import org.jboss.msc.servic
.Servi eActivator;
imp

t org. boss.shrinkwrap.api.asset.StringAsset;

mport  rg.jboss.shrinkwrap.api.spec.JavaArchive;
i
port o g.wildfly.swarm.container.Container;
impor
 org.w ldfly.swarm.container.Deployment;

imp
rt jav .util.ArrayList;
import java.util.List;

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
        this.archive.addAsServiceProvider(ServiceActivator.class, (Class[]) this.activators.toArray(new Class[]{}));
        addJBossDeploymentStructure();
        return this.archive;
    }

    private void addJBossDeploymentStructure() {
        StringAsset structureXml = new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  \n" +
                "<jboss-deployment-structure>  \n" +
                "    <deployment>  \n" +
                "         <dependencies>  \n" +
                "              <module name=\"APP\" slot=\"dependencies\"/>  \n" +
                "        </dependencies>  \n" +
                "    </deployment>  \n" +
                "</jboss-deployment-structure>\n");

        this.archive.addAsManifestResource(structureXml, "jboss-deployment-structure.xml");
    }
}
