package io.thorntail.testsuite.vertx;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import io.thorntail.test.ThorntailTestRunner;
import io.vertx.core.Vertx;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bob on 6/21/18.
 */
@RunWith(ThorntailTestRunner.class)
public class VertxTest {

    @Test
    public void testVertxObject() {
        assertThat( this.vertx ).isNotNull();
    }

    @Test
    public void testVerticleDeployment() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        this.vertx.eventBus().send( "verticle.a", "howdy", (response)->{
            String body = (String) response.result().body();
            assertThat( body ).isEqualTo( "got it: howdy");
            latch.countDown();
        });

        latch.await();
    }

    @Inject
    Vertx vertx;
}
