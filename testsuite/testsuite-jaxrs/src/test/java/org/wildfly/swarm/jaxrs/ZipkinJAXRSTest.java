package org.wildfly.swarm.jaxrs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

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
    public void testSpanLogging() {
        ResteasyClient client = (ResteasyClient) ResteasyClientBuilder.newClient();
        client.register(ClientRequestInterceptor.class);
        client.register(ClientResponseInterceptor.class);
        WebTarget target = client.target("http://localhost:8080");

        javax.ws.rs.core.Response response = target.request(MediaType.TEXT_PLAIN).get();
        Assert.assertEquals(200, response.getStatus());

        // check log file for span reporting
        // the default zipkin fraction logs to system out

        File file = new File(logFile);
        boolean conditionMet= false;

        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.contains("com.github.kristofa.brave.LoggingSpanCollector")) {
                    conditionMet=true;
                    break;
                }
            }
        } catch(FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Assert.assertTrue("Span logging missing from log file", conditionMet);


    }

}