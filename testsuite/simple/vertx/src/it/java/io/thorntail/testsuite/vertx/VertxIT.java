package io.thorntail.testsuite.vertx;

import javax.inject.Inject;

import io.thorntail.test.ThorntailTestRunner;
import io.vertx.resourceadapter.VertxEventBus;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 2/12/18.
 */
@RunWith(ThorntailTestRunner.class)
public class VertxIT {

    @Test
    public void test() throws InterruptedException {
        this.bus.send("driven.event.address", "one" );
        this.bus.send( "driven.event.address", "two" );
        this.bus.send( "driven.event.address", "three" );

        this.results.await(3, 5000);

        assertThat( results.get() ).hasSize(3);
        assertThat( results.get() ).contains("one");
        assertThat( results.get() ).contains("two");
        assertThat( results.get() ).contains("three");
    }

    @Inject
    VertxEventBus bus;

    @Inject
    Results results;
}
