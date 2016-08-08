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
package org.wildfly.swarm.container;

import org.junit.Ignore;

/**
 * @author Bob McWhirter
 */
@Ignore
public class DeploymentTest {

    /*
    @Test
    public void testDeploymentFailure() throws Exception {
        Swarm container = new Swarm();
        container.start();
        JARArchive a = ShrinkWrap.create(JARArchive.class, "bad-deployment.jar");
        a.addModule("com.i.do.no.exist");
        try {
            container.deploy(a);
            fail("should have throw a DeploymentException");
        } catch (DeploymentException e) {
            // expected and correct
            assertThat(e.getArchive()).isSameAs(a);
            assertThat(e.getMessage()).contains("org.jboss.modules.ModuleNotFoundException: com.i.do.no.exist:main");
        }
        container.stop();
    }

    @Test
    public void testDeploymentSuccess() throws Exception {
        Swarm container = new Swarm();
        container.start();
        JARArchive a = ShrinkWrap.create(JARArchive.class, "good-deployment.jar");
        a.add(EmptyAsset.INSTANCE, "nothing.xml");
        container.deploy(a);
        container.stop();
    }
    */
}
