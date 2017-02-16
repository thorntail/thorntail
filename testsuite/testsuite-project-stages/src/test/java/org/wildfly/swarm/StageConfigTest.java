package org.wildfly.swarm;

import java.net.URL;
import java.util.Properties;

import org.junit.Test;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.config.ConfigView;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class StageConfigTest {

    @Test
    public void testPropertyBasedConfigStagesFile() throws Exception {
        try {
            URL projectStages = getClass().getClassLoader().getResource("simple-project-stages.yml");
            System.setProperty(SwarmProperties.PROJECT_STAGE_FILE, projectStages.toExternalForm());
            Swarm swarm = new Swarm();
            swarm.initializeConfigView(new Properties());

            ConfigView view = swarm.configView();
            assertThat(view.resolve("foo.bar.baz").getValue()).isEqualTo("cheddar");

            StageConfig stageConfig = swarm.stageConfig();
            assertThat(stageConfig.resolve("foo.bar.baz").getValue()).isEqualTo("cheddar");
            
        } finally {
            System.clearProperty(SwarmProperties.PROJECT_STAGE_FILE);
        }
    }
}
