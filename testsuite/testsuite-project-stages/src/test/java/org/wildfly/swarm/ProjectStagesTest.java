package org.wildfly.swarm;

import java.net.URL;
import java.util.Properties;

import org.junit.Test;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.config.ConfigView;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ProjectStagesTest {

    @Test
    public void testPropertyBasedConfigStagesFile() throws Exception {
        try {
            URL projectStages = getClass().getClassLoader().getResource("simple-project-stages.yml");
            System.setProperty(SwarmProperties.PROJECT_STAGE_FILE, projectStages.toExternalForm());
            Swarm swarm = new Swarm();
            swarm.initializeConfigView(new Properties());

            ConfigView view = swarm.configView();

            assertThat(view.resolve("foo.bar.baz").getValue()).isEqualTo("cheddar");
        } finally {
            System.clearProperty(SwarmProperties.PROJECT_STAGE_FILE);
        }
    }

    @Test
    public void testPropertyBasedConfigStagesFileOnlyDefault() throws Exception {
        try {
            URL projectStages = getClass().getClassLoader().getResource("multi-project-stages.yml");
            System.setProperty(SwarmProperties.PROJECT_STAGE_FILE, projectStages.toExternalForm());
            Swarm swarm = new Swarm();
            swarm.initializeConfigView(new Properties());

            ConfigView view = swarm.configView();

            assertThat(view.resolve("foo.bar.baz").getValue()).isEqualTo("cheddar");
        } finally {
            System.clearProperty(SwarmProperties.PROJECT_STAGE_FILE);
        }
    }

    @Test
    public void testPropertyBasedConfigStagesFileAndSelectedStage() throws Exception {
        try {
            URL projectStages = getClass().getClassLoader().getResource("multi-project-stages.yml");
            System.setProperty(SwarmProperties.PROJECT_STAGE_FILE, projectStages.toExternalForm());
            System.setProperty(SwarmProperties.PROJECT_STAGE, "production");
            Swarm swarm = new Swarm();
            swarm.initializeConfigView(new Properties());

            ConfigView view = swarm.configView();

            assertThat(view.resolve("foo.bar.baz").getValue()).isEqualTo("brie");
        } finally {
            System.clearProperty(SwarmProperties.PROJECT_STAGE_FILE);
            System.clearProperty(SwarmProperties.PROJECT_STAGE);
        }
    }
}
