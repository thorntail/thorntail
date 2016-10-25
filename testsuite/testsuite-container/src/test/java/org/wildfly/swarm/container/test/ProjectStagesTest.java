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
package org.wildfly.swarm.container.test;

import org.junit.*;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.container.runtime.cdi.ProjectStageFactory;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Heiko Braun
 * @since 07/04/16
 */
public class ProjectStagesTest {

    @Before
    public void prepareStage() {
        InputStream in = ProjectStagesTest.class.getClassLoader().getResourceAsStream("project-stages.yml");
        testStages = new ProjectStageFactory().loadStages( in );
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
            assertEquals(6, properties.keySet().size());

            assertTrue("Property is missing", properties.keySet().contains("swarm.magic.enabled"));
            assertTrue("Property is missing", properties.keySet().contains(SwarmProperties.PORT_OFFSET));
            properties.entrySet().forEach(e -> System.out.println(e.getKey()+"="+e.getValue()));
        }

    }

    @Test
    public void testStageConfigurationDefaultLoading() throws Exception {

        System.setProperty(SwarmProperties.PROJECT_STAGE, "default");

        Swarm container = new Swarm();
        container.start();

        assertEquals("10", System.getProperty(SwarmProperties.PORT_OFFSET));
        assertEquals("some.where.com", System.getProperty("remote.hosts[0]"));

        container.stop();
    }

    @Test
    public void testStageConfigurationLoading() throws Exception {

        System.setProperty(SwarmProperties.PROJECT_STAGE, "development");

        Swarm container = new Swarm()
                .withStageConfig(
                        ProjectStagesTest.class.getClassLoader().getResource("project-stages.yml")
                );

        container.start();

        assertEquals("50", System.getProperty(SwarmProperties.PORT_OFFSET));
        assertEquals("DEBUG", System.getProperty("logger.level"));

        container.stop();
    }

    @Test
    public void testStageConfigurationUrlLoading() throws Exception {

        System.setProperty(SwarmProperties.PROJECT_STAGE, "development");

        ClassLoader cl = this.getClass().getClassLoader();
        URL stageConfig = cl.getResource("project-stages.yml");

        assert stageConfig != null : "Failed to load stage configuration";

        Swarm container = new Swarm()
                .withStageConfig(stageConfig);

        container.start();

        assertEquals("50", System.getProperty(SwarmProperties.PORT_OFFSET));
        assertEquals("DEBUG", System.getProperty("logger.level"));

        container.stop();
    }

    @Test
    public void testArgsPrecendence() throws Exception {

        System.setProperty(SwarmProperties.PROJECT_STAGE, "development");
        System.setProperty(SwarmProperties.PORT_OFFSET, "150");

        Swarm container = new Swarm()
                .withStageConfig(
                        ProjectStagesTest.class.getClassLoader().getResource("project-stages.yml")
                );
        container.start();

        assertEquals("150", System.getProperty(SwarmProperties.PORT_OFFSET));

        container.stop();

    }

    @Test
    public void testUnknownStageConfiguration() throws Exception {

        Swarm container = null;
        System.setProperty(SwarmProperties.PROJECT_STAGE, "foobar");
        try {
            container = new Swarm();
            container.withStageConfig(ProjectStagesTest.class.getClassLoader().getResource("project-stages.yml"));
            fail();
        } catch(RuntimeException ex) {
            // TODO
            assertEquals(true,ex.getMessage().contains("WFSWARM0003"));
        }

    }

    @Test
    public void testStageConfigAPI() throws Exception {

        Swarm container = new Swarm()
                .withStageConfig(
                        ProjectStagesTest.class.getClassLoader().getResource("project-stages.yml")
                );

        StageConfig stageConfig = container.stageConfig();
        assertNotNull(stageConfig);

        assertEquals("DEBUG", stageConfig.resolve("logger.level").getValue());

        Integer intVal = stageConfig
                .resolve(SwarmProperties.PORT_OFFSET)
                .as(Integer.class)
                .getValue();

        assertEquals(new Integer(10), intVal);

        Boolean boolVal = stageConfig
                .resolve("swarm.magic.enabled")
                .as(Boolean.class)
                .getValue();

        assertEquals(Boolean.TRUE, boolVal);

        container.start().stop();
    }

    private List<ProjectStage> testStages;
}
