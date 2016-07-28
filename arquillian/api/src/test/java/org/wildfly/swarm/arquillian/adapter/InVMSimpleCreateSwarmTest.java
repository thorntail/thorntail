/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.arquillian.adapter;

import org.fest.assertions.Assertions;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;

/**
 * @author alexsoto
 */
//TODO Fix this!
@Ignore
public class InVMSimpleCreateSwarmTest {

    static Swarm mockContainer = Mockito.mock(Swarm.class);

    @Test
    public void shouldCreateContainerUsingContainerAnnotation() throws Exception {
        final InVMSimpleContainer inVMSimpleContainer
                = new InVMSimpleContainer(InVMClassAnnotatedWithContainer.class);
        inVMSimpleContainer.start(ShrinkWrap.create(JavaArchive.class));

        Assertions.assertThat(Swarm.COMMAND_LINE_ARGS).isEqualTo(new String[] {"This is a test with container annotation"});
    }

    static class InVMClassAnnotatedWithContainer {
        @CreateSwarm
        public static Swarm mySwarm() throws Exception {
            Mockito.reset(mockContainer);
            Mockito.when(Swarm.COMMAND_LINE_ARGS).thenReturn(new String[]{"This is a test with container annotation"});
            Mockito.when(mockContainer.start()).thenReturn(mockContainer);

            return mockContainer;
        }

    }

}
