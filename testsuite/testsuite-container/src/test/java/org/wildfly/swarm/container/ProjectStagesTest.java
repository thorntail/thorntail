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

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.container.internal.ProjectStageFactory;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Heiko Braun
 * @since 07/04/16
 */
public class ProjectStagesTest {

    @Before
    public void prepareStage() {
        testStages = new ProjectStageFactory().loadStages(
                ProjectStagesTest.class.getClassLoader().getResourceAsStream("project-stages.yml")
        );
    }

    @After
    public void clearProps() {

        // cleanup the props used, otherwise they interfere with followup tests
        for (ProjectStage stage : testStages) {
            for (String key : stage.getProperties().keySet()) {
                System.clearProperty(key);
            }
        }

        System.clearProperty(SwarmProperties.PROJECT_STAGE);
    }

    @Test
    public void testParser() {

        ProjectStageFactory factory = new ProjectStageFactory();
        List<ProjectStage> stages = factory.loadStages(
                ProjectStagesTest.class.getClassLoader().getResourceAsStream("project-stages.yml")
        );

        Assert.assertEquals(3, stages.size());

        for (ProjectStage stage : stages) {
            System.out.println("["+stage.getName()+"]");
            Map<String, String> properties = stage.getProperties();
            Assert.assertEquals(6, properties.keySet().size());

            Assert.assertTrue("Property is missing", properties.keySet().contains("swarm.magic.enabled"));
            Assert.assertTrue("Property is missing", properties.keySet().contains(SwarmProperties.PORT_OFFSET));
            properties.entrySet().forEach(e -> System.out.println(e.getKey()+"="+e.getValue()));
        }

    }

    @Test
    public void testStageConfigurationLoading() throws Exception {

        System.setProperty(SwarmProperties.PROJECT_STAGE, "development");

        Container container = new Container()
                .withStageConfig(
                        ProjectStagesTest.class.getClassLoader().getResource("project-stages.yml")
                );
        container.start();


        Assert.assertEquals("50", System.getProperty(SwarmProperties.PORT_OFFSET));
        Assert.assertEquals("DEBUG", System.getProperty("logger.level"));

        container.stop();

    }


    @Test
    public void testArgsPrecendence() throws Exception {

        System.setProperty(SwarmProperties.PROJECT_STAGE, "development");
        System.setProperty(SwarmProperties.PORT_OFFSET, "150");

        Container container = new Container()
                .withStageConfig(
                        ProjectStagesTest.class.getClassLoader().getResource("project-stages.yml")
                );
        container.start();

        Assert.assertEquals("150", System.getProperty(SwarmProperties.PORT_OFFSET));

        container.stop();

    }

    @Test
    public void testFractionStageAccess() throws Exception {

        System.setProperty(SwarmProperties.PROJECT_STAGE, "development");

        Container container = new Container()
                .withStageConfig(
                        ProjectStagesTest.class.getClassLoader().getResource("project-stages.yml")
                )
                .fraction(new Fraction() {
                    @Override
                    public void initialize(InitContext initContext) {
                        Assert.assertTrue("stage config is not present", initContext.projectStage().isPresent());
                        StageConfig stageConfig = initContext.projectStage().get();
                        Assert.assertEquals("development", stageConfig.getName());
                        stageConfig.keys().contains("logger.level");
                    }
                });

        container.start().stop();
    }


    @Test
    @Ignore
    public void testUnknownStageConfiguration() throws Exception {

        System.setProperty(SwarmProperties.PROJECT_STAGE, "foobar");

        Container container = new Container();
        boolean yieldException = false;

        try {
            container.withStageConfig(
                    ProjectStagesTest.class.getClassLoader().getResource("project-stages.yml")
            );
        } catch (Throwable e) {
            yieldException = true;
        }

        container.start();
        Assert.assertTrue(yieldException);

        container.stop();

    }

    @Test
    public void testStageConfigAPI() throws Exception {

        Container container = new Container()
                .withStageConfig(
                        ProjectStagesTest.class.getClassLoader().getResource("project-stages.yml")
                )
                .fraction(new Fraction() {
                    @Override
                    public void initialize(InitContext initContext) {
                        Assert.assertTrue("stage config is not present", initContext.projectStage().isPresent());
                        StageConfig stageConfig = initContext.projectStage().get();
                        Assert.assertEquals("DEBUG", stageConfig.resolve("logger.level").getValue());

                        Integer intVal = stageConfig
                                .resolve(SwarmProperties.PORT_OFFSET)
                                .as(Integer.class)
                                .getValue();

                        Assert.assertEquals(new Integer(10), intVal);

                        Boolean boolVal = stageConfig
                                .resolve("swarm.magic.enabled")
                                .as(Boolean.class)
                                .getValue();

                        Assert.assertEquals(Boolean.TRUE, boolVal);
                    }
                });

        container.start().stop();
    }

    /**
     * Project stage shsould be discovered from classpath (modular or not)
     * See https://issues.jboss.org/browse/SWARM-486
     */
    @Test
    public void testStageConfigDiscovery() throws Exception {

        Container container = new Container()
                .fraction(new Fraction() {
                    @Override
                    public void initialize(InitContext initContext) {
                        Assert.assertTrue("stage config is not present", initContext.projectStage().isPresent());
                        StageConfig stageConfig = initContext.projectStage().get();
                    }
                });

        container.start().stop();
    }


    private List<ProjectStage> testStages;
}
