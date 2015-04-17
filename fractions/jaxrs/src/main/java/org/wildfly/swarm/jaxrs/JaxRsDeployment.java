package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.vfs.VirtualFile;
import org.wildfly.swarm.container.Deployment;
import org.wildfly.swarm.shrinkwrap.ShrinkWrapDeployment;

import javax.ws.rs.core.Application;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class JaxRsDeployment implements Deployment {

    private final ShrinkWrapDeployment<WebArchive> delegate;

    private Class<? extends Application> application;

    public JaxRsDeployment() {
        this.delegate = new ShrinkWrapDeployment<WebArchive>( archiveName(), WebArchive.class);
    }

    public JaxRsDeployment setApplication(Class<? extends Application> application) {
        this.application = application;
        return this;
    }

    public JaxRsDeployment addResource(Class resource) {
        return addClass( resource );
    }

    public JaxRsDeployment addClass(Class cls) {
        this.delegate.getArchive().addClass(cls);
        return this;
    }

    @Override
    public String getName() {
        return archiveName();
    }

    @Override
    public VirtualFile getContent() throws IOException {
        ensureApplication();
        addWebXml();
        addJBossDeploymentStructure();
        return this.delegate.getContent();
    }

    private void ensureApplication() {
        if (this.application != null) {
            this.delegate.getArchive().addClass(this.application);
        } else {
            this.delegate.getArchive().addClass(DefaultApplication.class);
        }
    }

    private void addWebXml() {
        StringAsset webXml = new StringAsset("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "<web-app version=\"3.0\" xmlns=\"http://java.sun.com/xml/ns/javaee\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\">\n" +
                "</web-app>\n");

        this.delegate.getArchive().addAsWebInfResource(webXml, "web.xml");
    }

    private void addJBossDeploymentStructure() {
        StringAsset structureXml = new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  \n" +
                "<jboss-deployment-structure>  \n" +
                "    <deployment>  \n" +
                "         <dependencies>  \n" +
                "              <module name=\"APP\" slot=\"dependencies\" />  \n" +
                "        </dependencies>  \n" +
                "    </deployment>  \n" +
                "</jboss-deployment-structure>\n");

        this.delegate.getArchive().addAsWebInfResource(structureXml, "jboss-deployment-structure.xml" );


    }

    private static String archiveName() {
        return System.getProperty( "wildfly.swarm.app.name" ).replace(".jar", ".war");
    }

}
