package org.wildfly.swarm;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.modules.MavenResolvers;
import org.wildfly.swarm.bootstrap.util.JarFileManager;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.config.ConfigView;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
@SuppressWarnings("deprecation")
public class StageConfigTest {

    @After
    public void tearDown() {
        try {
            JarFileManager.INSTANCE.close();
            MavenResolvers.close();
            TempFileManager.INSTANCE.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPropertyBasedConfigStagesFile() throws Exception {
        try {
            URL projectStages = getClass().getClassLoader().getResource("simple-project-stages.yml");
            System.setProperty(SwarmProperties.PROJECT_STAGE_FILE, projectStages.toExternalForm());
            Swarm swarm = new Swarm(new Properties());

            ConfigView view = swarm.configView();
            assertThat(view.resolve("foo.bar.baz").getValue()).isEqualTo("cheddar");

            StageConfig stageConfig = swarm.stageConfig();
            assertThat(stageConfig.resolve("foo.bar.baz").getValue()).isEqualTo("cheddar");

        } finally {
            System.clearProperty(SwarmProperties.PROJECT_STAGE_FILE);
        }
    }
}
