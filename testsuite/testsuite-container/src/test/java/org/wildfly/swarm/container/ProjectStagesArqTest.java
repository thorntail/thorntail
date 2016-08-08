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

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
public class ProjectStagesArqTest {

    @Deployment(testable = false)
    public static Archive createDeployment() throws Exception {
        JARArchive jarArchive = ShrinkWrap.create(JARArchive.class, "arqDeployment.jar");
        jarArchive.add(new ClassLoaderAsset("project-stages.yml", ProjectStagesArqTest.class.getClassLoader()), "project-stages.yml");

        return jarArchive;
    }

    /**
     * Test loading of project-stages.yml in arq test cases
     * See https://issues.jboss.org/browse/SWARM-486
     */
    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        return new Swarm()
                .component( ProjectStageInjectable.class );
    }

    @Test
    public void testNothing() {
        // nothing
    }

    @Pre
    @Singleton
    public static class ProjectStageInjectable implements Customizer {

        @Inject
        private Instance<ProjectStage> stage;

        @Override
        public void customize() {
            if ( this.stage.isUnsatisfied() ) {
                throw new AssertionError("project stages not present");
            }
        }
    }

}
