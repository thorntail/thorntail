package org.wildfly.swarm;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.modules.MavenResolvers;
import org.wildfly.swarm.bootstrap.util.JarFileManager;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.config.ConfigView;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ProjectStagesTest {

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
    public void testCLIBasedSelectedStage() throws Exception {
        Swarm swarm = new Swarm(new Properties(),
                "-S", "production");

        ConfigView view = swarm.configView();
        assertThat(view.resolve("foo.bar.baz").getValue()).isEqualTo("brie");
    }

    @Test
    public void testSwarmAPIToLoadConfig() throws Exception {
        Swarm swarm = new Swarm(new Properties());
        swarm.withProfile("foo");
        ConfigView view = swarm.configView();
        assertThat(view.resolve("swarm.myname").getValue()).isEqualTo("foo");
        swarm.withProfile("bar");
        assertThat(view.resolve("swarm.myname").getValue()).isEqualTo("foo");
        assertThat(view.resolve("swarm.mydottednumber").as(Double.class).getValue()).isEqualTo(2.82);
    }

    @Test
    public void testCLIToLoadConfig() throws Exception {
        Swarm swarm = new Swarm(new Properties(), "-Sfoo");
        ConfigView view = swarm.configView();
        assertThat(view.resolve("swarm.myname").getValue()).isEqualTo("foo");
    }

    @Test
    public void testEnvironmentVars() throws Exception {
        Map<String, String> environment = new HashMap<>();
        environment.put("swarm.myname", "from_env");
        Swarm swarm = new Swarm(new Properties(), environment);

        ConfigView view = swarm.configView();
        assertThat(view.resolve("swarm.myname").getValue()).isEqualTo("from_env");
    }

    @Test
    public void testPropertiesPreferredToEnvironmentVars() throws Exception {
        Map<String, String> environment = new HashMap<>();
        Properties properties = new Properties();

        environment.put("swarm.myname", "from_env");
        properties.setProperty("swarm.myname", "from_props");

        Swarm swarm = new Swarm(properties, environment);

        ConfigView view = swarm.configView();
        assertThat(view.resolve("swarm.myname").getValue()).isEqualTo("from_props");
    }

    @Test
    public void testPropertiesOnCLIPreferredToEnvironmentVars() throws Exception {
        Map<String, String> environment = new HashMap<>();
        Properties properties = new Properties();

        environment.put("swarm.myname", "from_env");
        properties.setProperty("swarm.myname", "from_props");

        Swarm swarm = new Swarm(properties, environment, "-Dswarm.myname=tacos");

        ConfigView view = swarm.configView();
        assertThat(view.resolve("swarm.myname").getValue()).isEqualTo("tacos");
    }

    @Test
    public void testPseudoPropertiesToSelectProjectStage() throws Exception {
        Swarm swarm = new Swarm(new Properties(),
                                "-Dswarm.project.stage=production");

        ConfigView view = swarm.configView();
        assertThat(view.resolve("foo.bar.baz").getValue()).isEqualTo("brie");
    }

    @Test
    public void testIsolatedPropertiesToSelectProjectStage() throws Exception {
        Properties props = new Properties();
        props.setProperty("swarm.project.stage", "production");
        Swarm swarm = new Swarm(props,
                                "-Dswarm.project.stage=production");

        ConfigView view = swarm.configView();
        assertThat(view.resolve("foo.bar.baz").getValue()).isEqualTo("brie");
        assertThat(view.resolve("foo.bar.taco").getValue()).isEqualTo("crunchy");
    }

}
