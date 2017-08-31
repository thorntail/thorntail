/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        when(env.getDependencies()).thenReturn(Collections.singleton("org.jboss.forge.addon:ui-spi:jar:3.4.0.Final"));

        ModuleSpec.Builder builder = mock(ModuleSpec.Builder.class);

        ApplicationModuleFinder sut = new ApplicationModuleFinder();
        sut.addDependencies(builder, env);

        verify(builder, times(1)).addResourceRoot(any());
    }

    @Test
    public void testDependencyHasClassifier() {
        // Mocks
        ApplicationEnvironment env = mock(ApplicationEnvironment.class);
        when(env.getDependencies()).thenReturn(Collections.singleton("org.jboss.forge.addon:ui-spi:jar:forge-addon:3.4.0.Final"));

        ModuleSpec.Builder builder = mock(ModuleSpec.Builder.class);

        ApplicationModuleFinder sut = new ApplicationModuleFinder();
        sut.addDependencies(builder, env);

        verify(builder, times(1)).addResourceRoot(any());
    }
}
