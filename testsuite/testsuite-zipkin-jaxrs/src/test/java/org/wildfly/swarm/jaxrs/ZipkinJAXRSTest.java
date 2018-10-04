package org.wildfly.swarm.jaxrs;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
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

    private static final String LOG_FILE = System.getProperty("user.dir") + File.separator + "swarm-test.log";
    private static final String SERVICE_NAME = "thorntail-service";
    private static final String SPAN_COLLECTOR = "com.github.kristofa.brave.LoggingSpanCollector";


    @Deployment
    public static Archive createDeployment() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("project-defaults.yml");
        File projectDefaults = new File(url.toURI());
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "myapp.war");
        deployment.addResource(MyResource.class);
        deployment.addAllDependencies();
        deployment.addAsResource(projectDefaults, "/project-defaults.yml");
        return deployment;
    }

    // Unable to remove @CreateSwarm at present as we need a way to track the log file name
    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        System.out.println("Log file: " + LOG_FILE);

        return new Swarm()
                .fraction(
                        new LoggingFraction()
                                .defaultColorFormatter()
                                .consoleHandler(Level.INFO, "COLOR_PATTERN")
                                .fileHandler("FILE", f -> {

                                    Map<String, String> fileProps = new HashMap<>();
                                    fileProps.put("path", LOG_FILE);
                                    f.file(fileProps);
                                    f.level(Level.INFO);
                                    f.formatter("%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");

                                })
                                .rootLogger(Level.INFO, "CONSOLE", "FILE")
                );
    }

    @Test
    public void testSpanLogging() throws Exception {
        ResteasyClient client = (ResteasyClient) ResteasyClientBuilder.newClient();
        client.register(ClientRequestInterceptor.class);
        client.register(ClientResponseInterceptor.class);

        Response response = client.target("http://localhost:8080").request(MediaType.TEXT_PLAIN).get();
        Assert.assertEquals(200, response.getStatus());

        // check log file for span reporting & the specified service name
        // the default zipkin fraction logs to system out

        List<String> logContent = Files.readAllLines(Paths.get(LOG_FILE));
        boolean spanPresent = logContent.stream().anyMatch(line -> line.contains(SPAN_COLLECTOR));
        Assert.assertTrue("Span logging missing from log file", spanPresent);

        boolean serviceNamePresent = logContent.stream().anyMatch(line -> line.contains(SERVICE_NAME));
        Assert.assertTrue("Service name " + SERVICE_NAME + " missing from log file", serviceNamePresent);
    }
}
