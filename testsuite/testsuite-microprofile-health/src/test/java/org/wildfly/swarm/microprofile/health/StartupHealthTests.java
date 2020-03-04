package org.wildfly.swarm.microprofile.health;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(Arquillian.class)
public class StartupHealthTests {

    private static final String APP_NAME = "test";

    private Client client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @ArquillianResource
    private URL baseUrl;

    @ArquillianResource
    private Deployer deployer;

    @Before
    public void before() {
        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        if (client != null) {
            client.close();
        }
    }

    @Deployment(name = APP_NAME, managed = false)
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addPackage(TestDownReadinessHC.class.getPackage());
    }

    @Test
    @InSequence(1)
    public void testLivenessProbeDuringStartup() throws ExecutionException, InterruptedException {
        Assert.assertNull(getLiveness());

        Future<Response> checkTask = executor.submit(waitForNonNull(this::getLiveness));

        deployer.deploy(APP_NAME);

        // wait for the first response from the liveness probe
        Response response = checkTask.get();

        Assert.assertEquals(200, response.getStatus());
        String entity = response.readEntity(String.class);
        Assert.assertTrue(entity.contains("UP"));
        Assert.assertTrue(entity.contains("[]"));

        // wait for the first non-empty response
        String result = executor.submit(waitForNonNull(() -> {
            String liveness = getLiveness().readEntity(String.class);
            return liveness.contains("[]") ? null : liveness;
        })).get();

        Assert.assertTrue(result.contains("UP"));
        Assert.assertTrue(result.contains(TestUpLivenessHC.class.getSimpleName()));

        deployer.undeploy(APP_NAME);
    }

    @Test
    @InSequence(2)
    public void testReadinessProbeDuringStartup() throws ExecutionException, InterruptedException {
        Assert.assertNull(getReadiness());

        Future<Response> checkTask = executor.submit(waitForNonNull(this::getReadiness));

        deployer.deploy(APP_NAME);

        // wait for the first response from the readiness probe
        Response response = checkTask.get();

        Assert.assertEquals(503, response.getStatus());
        String entity = response.readEntity(String.class);
        Assert.assertTrue(entity.contains("DOWN"));
        Assert.assertTrue(entity.contains("[]"));

        // wait for the first non-empty response
        String result = executor.submit(waitForNonNull(() -> {
            String readiness = getReadiness().readEntity(String.class);
            return readiness.contains("[]") ? null : readiness;
        })).get();

        Assert.assertTrue(result.contains("DOWN"));
        Assert.assertTrue(result.contains(TestDownReadinessHC.class.getSimpleName()));

        deployer.undeploy(APP_NAME);
    }

    private <T> Callable<T> waitForNonNull(Callable<T> waitFunction) {
        return () -> {
            T response = null;

            while ((response = waitFunction.call()) == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            return response;
        };
    }

    private Response getReadiness() {
        return getHealth("/health/ready");
    }

    private Response getLiveness() {
        return getHealth("/health/live");
    }

    private Response getHealth(String path) {
        try {
            return client.target(baseUrl.toString())
                .path(path)
                .request()
                .get();
        } catch (Exception e) {
            // expected by the tests
            return null;
        }
    }
}
