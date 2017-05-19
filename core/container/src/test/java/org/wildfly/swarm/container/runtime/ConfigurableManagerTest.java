package org.wildfly.swarm.container.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.wildfly.swarm.container.config.ConfigViewFactory;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContext;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContextImpl;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.config.ConfigView;

import static org.fest.assertions.Assertions.*;

/**
 * Created by bob on 5/19/17.
 */
public class ConfigurableManagerTest {

    @Test
    public void testDeploymentConfigurable() throws Exception {
        Properties props = new Properties();
        Map<String, String> env = new HashMap<>();
        ConfigViewFactory factory = new ConfigViewFactory(props, env);
        factory.withProperty("swarm.deployment.[myapp.war].context", "/myapp");
        ConfigView configView = factory.get(true);
        DeploymentContext context = new DeploymentContextImpl();
        ConfigurableManager manager = new ConfigurableManager(configView, context);

        Archive archive = ShrinkWrap.create(JavaArchive.class, "myapp.war");
        try {
            context.activate(archive);
            Component component = new Component();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/myapp");

        } finally {
            context.deactivate();
        }
    }

    @Test
    public void testDeploymentConfigurableUsingAlias() throws Exception {
        Properties props = new Properties();
        Map<String, String> env = new HashMap<>();
        ConfigViewFactory factory = new ConfigViewFactory(props, env);
        factory.withProperty("swarm.http.context", "/myapp");
        ConfigView configView = factory.get(true);
        DeploymentContext context = new DeploymentContextImpl();
        ConfigurableManager manager = new ConfigurableManager(configView, context);

        Archive archive = ShrinkWrap.create(JavaArchive.class, "myapp.war");
        try {
            context.activate(archive);
            Component component = new Component();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/myapp");

        } finally {
            context.deactivate();
        }
    }

    @Test
    public void testDeploymentConfigurableUsingBoth() throws Exception {
        Properties props = new Properties();
        Map<String, String> env = new HashMap<>();
        ConfigViewFactory factory = new ConfigViewFactory(props, env);
        factory.withProperty("swarm.deployment.[myapp.war].context", "/my-specific-app");
        factory.withProperty("swarm.http.context", "/my-alias-app");
        ConfigView configView = factory.get(true);
        DeploymentContext context = new DeploymentContextImpl();
        ConfigurableManager manager = new ConfigurableManager(configView, context);

        Archive archive = ShrinkWrap.create(JavaArchive.class, "myapp.war");
        try {
            context.activate(archive);
            Component component = new Component();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/my-specific-app");
        } finally {
            context.deactivate();
        }

        Archive archiveToo = ShrinkWrap.create(JavaArchive.class, "otherapp.war");
        try {
            context.activate(archiveToo);
            Component component = new Component();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/my-alias-app");
        } finally {
            context.deactivate();
        }
    }

    public static class Component {
        @Configurable("swarm.deployment.*.context")
        @Configurable("swarm.http.context")
        public Defaultable<String> context = Defaultable.string("/");
    }
}
