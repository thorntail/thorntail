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
            Swarm swarm = new Swarm(new Properties());
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
            Swarm swarm = new Swarm(new Properties());

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
            Swarm swarm = new Swarm(new Properties());

            ConfigView view = swarm.configView();

            assertThat(view.resolve("foo.bar.baz").getValue()).isEqualTo("brie");
        } finally {
            System.clearProperty(SwarmProperties.PROJECT_STAGE_FILE);
            System.clearProperty(SwarmProperties.PROJECT_STAGE);
        }
    }

    @Test
    public void testSwarmAPIToLoadConfig() throws Exception {
        Swarm swarm = new Swarm( new Properties());
        swarm.withProfile("foo");
        ConfigView view = swarm.configView();
        assertThat(view.resolve("myname").getValue()).isEqualTo("foo");
        swarm.withProfile("bar");
        assertThat(view.resolve("myname").getValue()).isEqualTo("foo");
        assertThat(view.resolve("mydottednumber").as(Double.class).getValue()).isEqualTo(2.82);
    }

    @Test
    public void testCLIToLoadConfig() throws Exception {
        Swarm swarm = new Swarm(new Properties(), "-Sfoo");
        ConfigView view = swarm.configView();
        assertThat(view.resolve("myname").getValue()).isEqualTo("foo");
    }

}
