package org.eclipse.microprofile.restclient.wfswarm.arquillian;

import java.io.File;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.wildfly.swarm.undertow.WARArchive;

public class RestClientArchiveProcessor implements ApplicationArchiveProcessor {

    private static final Logger log = Logger.getLogger(RestClientArchiveProcessor.class.getName());

    @Override
    public void process(Archive<?> appArchive, TestClass testClass) {

        if (!(appArchive instanceof WebArchive)) {
            return;
        }
        log.info("Preparing archive: " + appArchive);
        WARArchive war = appArchive.as(WARArchive.class);

        PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile("pom.xml");

        // Wiremock classes that need to be present
        File[] wiremockDeps = pom
                .resolve("com.github.tomakehurst:wiremock")
                .withTransitivity()
                .asFile();

        war.addAsLibraries(wiremockDeps);


        // TCK Classes that need to present
        war.addPackages(true, "org.eclipse.microprofile.rest.client.tck.ext");

        log.fine("Augmented war: \n" + war.toString(true));
    }
}
