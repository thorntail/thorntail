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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
public class ProjectStagesArqTest implements ContainerFactory {

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
    @Override
    public Container newContainer(String... args) throws Exception {

        return new Container().fraction(new Fraction() {
            @Override
            public void initialize(InitContext initContext) {
                // does nothing but extract stageConfig for verification
                if (!initContext.projectStage().isPresent()) {
                    throw new AssertionError("project stages not present");
                }
            }
        });

    }

    @Test
    public void testStageConfigPresence() {
        // nada
    }

}
