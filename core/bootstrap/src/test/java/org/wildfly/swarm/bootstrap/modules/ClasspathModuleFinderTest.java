package org.wildfly.swarm.bootstrap.modules;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleSpec;
import org.junit.Test;

/**
 * @author Benedikt Biallowons <benedikt@biallowons.de>
 *
 */
public class ClasspathModuleFinderTest {
    @Test
    public void testMain() {
        ClasspathModuleFinder finder = new ClasspathModuleFinder();

        try {
            ModuleSpec spec = finder.findModule("classpath.module.load.test", null);

            assertNotNull(spec);
        } catch (ModuleLoadException e) {
            fail();
        }
    }

    @Test
    public void testMissingMain() {
        ClasspathModuleFinder finder = new ClasspathModuleFinder();

        try {
            ModuleSpec spec = finder.findModule("classpath.module.load.test.missing", null);

            assertNull(spec);
        } catch (ModuleLoadException e) {
            fail();
        }
    }

    @Test
    public void testSlot() {
        ClasspathModuleFinder finder = new ClasspathModuleFinder();

        try {
            ModuleSpec spec = finder.findModule("classpath.module.load.test:1.0.0.Final", null);

            assertNotNull(spec);
        } catch (ModuleLoadException e) {
            fail();
        }
    }

    @Test
    public void testMissingSlot() {
        ClasspathModuleFinder finder = new ClasspathModuleFinder();

        try {
            ModuleSpec spec = finder.findModule("classpath.module.load.test:missing", null);

            assertNull(spec);
        } catch (ModuleLoadException e) {
            fail();
        }
    }

}
