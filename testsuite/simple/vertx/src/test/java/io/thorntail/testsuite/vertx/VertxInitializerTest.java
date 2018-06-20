package io.thorntail.testsuite.vertx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.thorntail.Thorntail;
import io.thorntail.test.ThorntailTestRunner;
import io.vertx.core.Vertx;

@RunWith(ThorntailTestRunner.class)
public class VertxInitializerTest {

    static final String DUMMY = "dummy";

    static final String BUNNY = "bunny";

    @Test
    public void testObservers() throws InterruptedException {
        Instance<Object> instance = Thorntail.current().getBeanManager().createInstance();
        Emitter emitter = instance.select(Emitter.class).get();
        Results results = instance.select(Results.class).get();
        results.reset(4);

        // Consumed by Receiver#onDummy()
        emitter.emit(DUMMY, "foo");
        emitter.emit(DUMMY, "bar");
        // Consumed by InjectedVerticle and Receiver#onBunny()
        emitter.emitBunny("baz");

        results.await();

        List<Object> messages = results.getMessages();
        assertEquals(4, messages.size());
        assertTrue(messages.contains(DUMMY + "foo"));
        assertTrue(messages.contains(DUMMY + "bar"));
        assertTrue(messages.contains(BUNNY + "baz"));
        assertTrue(messages.contains(InjectedVerticle.class.getSimpleName() + "baz"));
    }

    @Test
    public void testVertxConfig() {
        Vertx vertx = Thorntail.current().getBeanManager().createInstance().select(Vertx.class).get();
        assertTrue(vertx.isClustered());
    }

}