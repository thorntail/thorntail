package org.wildfly.swarm.container;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.swarm.container.runtime.cdi.ProjectStageFactory;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.StageConfig;

/**
 * @author Heiko Braun
 * @since 16/06/16
 */
public class ProjectStageTest {

    /**
     * Stage config values should be inherited from the default if not explicitly given
     * in a named stage. See also https://issues.jboss.org/browse/SWARM-503
     * @throws Exception
     */
    @Test
    public void testInheritance() throws Exception {
        InputStream inputStream = ProjectStageTest.class.getClassLoader().getResourceAsStream("test-stages.yml");
        Assert.assertNotNull("Cannot find test-stages.yml", inputStream);
        List<ProjectStage> projectStages = new ProjectStageFactory().loadStages(inputStream);
        Assert.assertEquals(2, projectStages.size());

        StageConfig defaultConfig = new StageConfig(projectStages.get(0));
        StageConfig otherConfig = new StageConfig(projectStages.get(1));
        Assert.assertEquals("default", defaultConfig.getName());
        Assert.assertEquals("other-stage", otherConfig.getName());

        Assert.assertEquals(
                "a", defaultConfig
                        .resolve("some.prop.value")
                        .getValue()
        );

        Assert.assertEquals(
                "b", defaultConfig
                        .resolve("another.prop.value")
                        .getValue()
        );

        Assert.assertEquals(
                "c", otherConfig
                        .resolve("some.prop.value")
                        .getValue()
        );

        Assert.assertEquals(
                "Expected that 'another.prop.value' is inherited from default stage",
                "b",
                otherConfig
                        .resolve("another.prop.value")
                        .withDefault("n/a")
                        .getValue()
        );

    }
}
