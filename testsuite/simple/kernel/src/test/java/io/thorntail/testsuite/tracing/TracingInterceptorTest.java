package io.thorntail.testsuite.tracing;

import static org.junit.Assert.assertEquals;

import javax.enterprise.inject.Instance;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.thorntail.Thorntail;
import io.thorntail.test.ThorntailTestRunner;

@RunWith(ThorntailTestRunner.class)
public class TracingInterceptorTest {

    @Test
    public void testSpanHandlers() {
        AlphaHandler.HANDLED_COUNTER.set(0);
        AlphaHandler.CREATED_COUNTER.set(0);
        BravoHandler.HANDLED_COUNTER.set(0);

        Instance<TracedComponent> instance = Thorntail.current().getBeanManager().createInstance().select(TracedComponent.class);
        TracedComponent component = instance.get();

        component.alpha();
        component.alpha();
        // AlphaHandler can handle and has higher priority than the default handler
        assertEquals(2, AlphaHandler.HANDLED_COUNTER.get());
        assertEquals(0, BravoHandler.HANDLED_COUNTER.get());
        // TracingInterceptor - the cache was used
        assertEquals(1, AlphaHandler.CREATED_COUNTER.get());

        component.bravo();
        component.bravo();
        // AlphaHandler and BravoHandler can handle but BravoHandler has higher priority
        assertEquals(2, AlphaHandler.HANDLED_COUNTER.get());
        assertEquals(2, BravoHandler.HANDLED_COUNTER.get());
    }

}
