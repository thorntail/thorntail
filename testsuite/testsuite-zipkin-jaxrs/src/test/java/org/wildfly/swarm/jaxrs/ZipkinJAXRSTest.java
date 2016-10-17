package org.wildfly.swarm.jaxrs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.jaxrs.btm.ZipkinFraction;
import org.wildfly.swarm.jaxrs.btm.zipkin.ClientRequestInterceptor;
import org.wildfly.swarm.jaxrs.btm.zipkin.ClientResponseInterceptor;
import org.wildfly.swarm.logging.LoggingFraction;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
public class ZipkinJAXRSTest {

    static String logFile = System.getProperty("user.dir") + File.separator + "swarm-test.log";

    @Deployment
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(MyResource.class);
        deployment.addAllDependencies();
        return deployment;
    }

    @CreateSwarm
    public static Swarm newContainer() throws Exception {


        System.out.println("Log file: " + logFile);

        return new Swarm()
                .fraction(
                        new LoggingFraction()
                                .defaultColorFormatter()
                                .consoleHandler(Level.INFO, "COLOR_PATTERN")
                                .fileHandler("FILE", f -> {

                                    Map<String, String> fileProps = new HashMap<>();
                                    fileProps.put("path", logFile);
                                    f.file(fileProps);
                                    f.level(Level.INFO);
                                    f.formatter("%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");

                                })
                                .rootLogger(Level.INFO, "CONSOLE", "FILE")
                )
                .fraction(new ZipkinFraction());
    }

    @Test
    public void testSpanLogging() throws Exception {
        ResteasyClient client = (ResteasyClient) ResteasyClientBuilder.newClient();
        client.register(ClientRequestInterceptor.class);
        client.register(ClientResponseInterceptor.class);

        Response response = client.target("http://localhost:8080").request(MediaType.TEXT_PLAIN).get();
        Assert.assertEquals(200, response.getStatus());

        // check log file for span reporting
        // the default zipkin fraction logs to system out

        boolean conditionMet = Files.readAllLines(Paths.get(logFile)).stream().anyMatch(line -> line.contains("com.github.kristofa.brave.LoggingSpanCollector"));
        Assert.assertTrue("Span logging missing from log file", conditionMet);
    }

}