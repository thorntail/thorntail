package io.thorntail.testsuite.activeinstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.thorntail.ActiveInstance;
import io.thorntail.Thorntail;
import io.thorntail.test.ThorntailTestRunner;

@RunWith(ThorntailTestRunner.class)
public class ActiveInstanceTest {

    @Test
    public void testActivate() {
        Bla.DESTROYED.set(false);
        ActiveInstance<Blu> activeBlu = Thorntail.current().activate(new Blu());
        // Blu is intercepted
        assertEquals("yak+foo", activeBlu.get().blabla());
        activeBlu.release();
        // Bla is correctly destroyed
        assertTrue(Bla.DESTROYED.get());
    }

    @Test
    public void testInstance() {
        Bla.DESTROYED.set(false);
        ActiveInstance<Blu> activeBlu = Thorntail.current().instance(Blu.class);
        // Blu is intercepted
        assertEquals("yak+foo", activeBlu.get().blabla());
        activeBlu.release();
        // Bla is correctly destroyed
        assertTrue(Bla.DESTROYED.get());
    }

}