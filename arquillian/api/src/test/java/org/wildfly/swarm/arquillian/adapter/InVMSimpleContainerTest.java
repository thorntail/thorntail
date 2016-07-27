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
import org.hamcrest.CoreMatchers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.arquillian.adapter.InVMSimpleContainer;
import org.wildfly.swarm.container.Container;

/**
 * @author alexsoto
 */
@Ignore
public class InVMSimpleContainerTest {

    static Container mockContainer = Mockito.mock(Container.class);

    @Test
    public void shouldCreateContainerUsingContainerFactory() throws Exception {

        final InVMSimpleContainer inVMSimpleContainer
                = new InVMSimpleContainer(InVMClassAsContainerFactory.class);
        inVMSimpleContainer.start(ShrinkWrap.create(JavaArchive.class));

//        Assertions.assertThat(mockContainer.getArgs()).isEqualTo(new String[] {"This is a test with container factory"});
    }

    @Test
    public void shouldCreateContainerUsingContainerAnnotation() throws Exception {
        final InVMSimpleContainer inVMSimpleContainer
                = new InVMSimpleContainer(InVMClassAnnotatedWithContainer.class);
        inVMSimpleContainer.start(ShrinkWrap.create(JavaArchive.class));

//        Assertions.assertThat(mockContainer.getArgs()).isEqualTo(new String[] {"This is a test with container annotation"});
    }

    @Test
    public void shouldCreateContainerUsingContainerFactoryAnnotation() throws Exception {
        final InVMSimpleContainer inVMSimpleContainer
                = new InVMSimpleContainer(InVMClassAnnotatedWithContainerFactory.class);
        inVMSimpleContainer.start(ShrinkWrap.create(JavaArchive.class));

//        Assertions.assertThat(mockContainer.getArgs()).isEqualTo(new String[] {"This is a test with container factory annotation"});
    }

    static class MockedContainerFactoryAnnotation implements ContainerFactory {

        @Override
        public Container newContainer(String... args) throws Exception {
            Mockito.reset(mockContainer);
//            Mockito.when(mockContainer.getArgs()).thenReturn(new String[]{"This is a test with container factory annotation"});
            Mockito.when(mockContainer.start()).thenReturn(mockContainer);
            return mockContainer;
        }
    }


    static class InVMClassAnnotatedWithContainerFactory {

        @org.wildfly.swarm.arquillian.adapter.ContainerFactory
        public static Class<? extends ContainerFactory> getContainerFactory() {
            return MockedContainerFactoryAnnotation.class;
        }
    }

    static class InVMClassAnnotatedWithContainer {

        @org.wildfly.swarm.arquillian.adapter.Container
        public static Container getContainer() throws Exception {
            Mockito.reset(mockContainer);
//            Mockito.when(mockContainer.getArgs()).thenReturn(new String[]{"This is a test with container annotation"});
            Mockito.when(mockContainer.start()).thenReturn(mockContainer);

            return mockContainer;
        }

    }

    static class InVMClassAsContainerFactory implements ContainerFactory {

        @Override
        public Container newContainer(String... args) throws Exception {
            Mockito.reset(mockContainer);
//            Mockito.when(mockContainer.getArgs()).thenReturn(new String[]{"This is a test with container factory"});
            Mockito.when(mockContainer.start()).thenReturn(mockContainer);

            return mockContainer;
        }
    }

}
