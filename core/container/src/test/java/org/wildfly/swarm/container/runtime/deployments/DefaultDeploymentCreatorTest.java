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
package org.wildfly.swarm.container.runtime.deployments;

import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.wildfly.swarm.spi.api.DefaultDeploymentFactory;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class DefaultDeploymentCreatorTest {

    @Test
    public void testNoDeploymentFactories() throws Exception {
        DefaultDeploymentCreator creator = new DefaultDeploymentCreator();
        DefaultDeploymentFactory factory = creator.getFactory("foo");
        assertThat(factory).isNull();
    }

    @Test
    public void testDistinctFactories() throws Exception {
        DefaultDeploymentCreator creator = new DefaultDeploymentCreator(
                new MockDefaultDeploymentFactory("war", 0),
                new MockDefaultDeploymentFactory("jar", 0)
        );

        DefaultDeploymentFactory jarFactory = creator.getFactory("jar");
        assertThat(jarFactory).isNotNull();
        assertThat(jarFactory.getType()).isEqualTo("jar");

        DefaultDeploymentFactory warFactory = creator.getFactory("war");
        assertThat(warFactory).isNotNull();
        assertThat(warFactory.getType()).isEqualTo("war");

        DefaultDeploymentFactory fooFactory = creator.getFactory("foo");
        assertThat(fooFactory).isNull();
    }

    @Test
    public void testConflictingFactories() throws Exception {
        MockDefaultDeploymentFactory lowPrio = new MockDefaultDeploymentFactory("war", 0);
        MockDefaultDeploymentFactory highPrio = new MockDefaultDeploymentFactory("war", 1000);

        DefaultDeploymentCreator creator = new DefaultDeploymentCreator( lowPrio, highPrio );
        DefaultDeploymentFactory factory = creator.getFactory( "war" );
        assertThat( factory ).isSameAs( highPrio );

        // try reverse registration

        creator = new DefaultDeploymentCreator( highPrio, lowPrio );
        factory = creator.getFactory( "war" );
        assertThat( factory ).isSameAs( highPrio );
    }
}
