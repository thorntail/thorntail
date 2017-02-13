package org.wildfly.swarm.drools.server.runtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class DroolsDeploymentProducer {

    private static String configFolder = System.getProperty("org.drools.server.swarm.web.conf");

    @Produces
    public Archive droolsWar() throws Exception {
        if (System.getProperty("org.drools.server.swarm.web.conf") == null) {
            try {
                //Path dir = Files.createTempDirectory("swarm-keycloak-config");
                File dir = TempFileManager.INSTANCE.newTempDirectory("swarm-drools-web-config", ".d");
                System.setProperty("org.drools.server.swarm.conf", dir.getAbsolutePath());
                Files.copy(getClass().getClassLoader().getResourceAsStream("config/web/web.xml"),
                        dir.toPath().resolve("web.xml"),
                        StandardCopyOption.REPLACE_EXISTING);
                Files.copy(getClass().getClassLoader().getResourceAsStream("config/web/jboss-web.xml"),
                        dir.toPath().resolve("jboss-web.xml"),
                        StandardCopyOption.REPLACE_EXISTING);
                configFolder = dir.toPath().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\tConfiguration folder is " + configFolder);

        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "drools-server.war");
        deployment.addAllDependencies();

        deployment.addAsWebInfResource(new File(configFolder + "/web.xml"), "web.xml");
        deployment.addAsWebInfResource(new File(configFolder + "/jboss-web.xml"), "jboss-web.xml");

        return deployment;
    }
}
