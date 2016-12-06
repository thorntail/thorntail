package org.wildfly.swarm.bootstrap.modules;

import java.util.Collections;

import org.jboss.modules.ModuleSpec;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test {@link ApplicationModuleFinder}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ApplicationModuleFinderTest {

    @Test
    public void testDependency() {
        // Mocks
        ApplicationEnvironment env = mock(ApplicationEnvironment.class);
        when(env.getDependencies()).thenReturn(Collections.singletonList("org.jboss.forge.addon:ui-spi:jar:3.4.0.Final"));

        ModuleSpec.Builder builder = mock(ModuleSpec.Builder.class);

        ApplicationModuleFinder sut = new ApplicationModuleFinder();
        sut.addDependencies(builder, env);

        verify(builder, times(1)).addResourceRoot(any());
    }

    @Test
    public void testDependencyHasClassifier() {
        // Mocks
        ApplicationEnvironment env = mock(ApplicationEnvironment.class);
        when(env.getDependencies()).thenReturn(Collections.singletonList("org.jboss.forge.addon:ui-spi:jar:forge-addon:3.4.0.Final"));

        ModuleSpec.Builder builder = mock(ModuleSpec.Builder.class);

        ApplicationModuleFinder sut = new ApplicationModuleFinder();
        sut.addDependencies(builder, env);

        verify(builder, times(1)).addResourceRoot(any());
    }
}
