/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
        factory.withProperty("thorntail.deployment.[myapp.war].context", "/myapp");
        ConfigView configView = factory.get(true);
        DeploymentContext context = new DeploymentContextImpl();
        ConfigurableManager manager = new ConfigurableManager(configView, context);

        Archive archive = ShrinkWrap.create(JavaArchive.class, "myapp.war");
        try {
            context.activate(archive, archive.getName(), false);
            Component component = new Component();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/myapp");

        } finally {
            context.deactivate();
        }
    }

    @Test
    public void testDeploymentConfigurableBackwardCompatibility() throws Exception {
        Properties props = new Properties();
        Map<String, String> env = new HashMap<>();
        ConfigViewFactory factory = new ConfigViewFactory(props, env);
        factory.withProperty("swarm.deployment.[myapp.war].context", "/myapp");
        ConfigView configView = factory.get(true);
        DeploymentContext context = new DeploymentContextImpl();
        ConfigurableManager manager = new ConfigurableManager(configView, context);

        Archive archive = ShrinkWrap.create(JavaArchive.class, "myapp.war");
        try {
            context.activate(archive, archive.getName(), false);
            BackwardCompatibleComponent component = new BackwardCompatibleComponent();
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
        factory.withProperty("thorntail.http.context", "/myapp");
        ConfigView configView = factory.get(true);
        DeploymentContext context = new DeploymentContextImpl();
        ConfigurableManager manager = new ConfigurableManager(configView, context);

        Archive archive = ShrinkWrap.create(JavaArchive.class, "myapp.war");
        try {
            context.activate(archive, archive.getName(), false);
            Component component = new Component();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/myapp");

        } finally {
            context.deactivate();
        }
    }

    @Test
    public void testDeploymentConfigurableUsingAliasBackwardCompatibility() throws Exception {
        Properties props = new Properties();
        Map<String, String> env = new HashMap<>();
        ConfigViewFactory factory = new ConfigViewFactory(props, env);
        factory.withProperty("swarm.http.context", "/myapp");
        ConfigView configView = factory.get(true);
        DeploymentContext context = new DeploymentContextImpl();
        ConfigurableManager manager = new ConfigurableManager(configView, context);

        Archive archive = ShrinkWrap.create(JavaArchive.class, "myapp.war");
        try {
            context.activate(archive, archive.getName(), false);
            BackwardCompatibleComponent component = new BackwardCompatibleComponent();
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
        factory.withProperty("thorntail.deployment.[myapp.war].context", "/my-specific-app");
        factory.withProperty("thorntail.http.context", "/my-alias-app");
        ConfigView configView = factory.get(true);
        DeploymentContext context = new DeploymentContextImpl();
        ConfigurableManager manager = new ConfigurableManager(configView, context);

        Archive archive = ShrinkWrap.create(JavaArchive.class, "myapp.war");
        try {
            context.activate(archive, archive.getName(), false);
            Component component = new Component();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/my-specific-app");
        } finally {
            context.deactivate();
        }

        Archive archiveToo = ShrinkWrap.create(JavaArchive.class, "otherapp.war");
        try {
            context.activate(archiveToo, archiveToo.getName(), false);
            Component component = new Component();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/my-alias-app");
        } finally {
            context.deactivate();
        }
    }

    @Test
    public void testDeploymentConfigurableUsingBothBackwardCompatible() throws Exception {
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
            context.activate(archive, archive.getName(), false);
            BackwardCompatibleComponent component = new BackwardCompatibleComponent();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/my-specific-app");
        } finally {
            context.deactivate();
        }

        Archive archiveToo = ShrinkWrap.create(JavaArchive.class, "otherapp.war");
        try {
            context.activate(archiveToo, archiveToo.getName(), false);
            BackwardCompatibleComponent component = new BackwardCompatibleComponent();
            manager.scan(component);
            assertThat(component.context.get()).isEqualTo("/my-alias-app");
        } finally {
            context.deactivate();
        }
    }

    public static class Component {
        @Configurable("thorntail.deployment.*.context")
        @Configurable("thorntail.http.context")
        public Defaultable<String> context = Defaultable.string("/");
    }

    public static class BackwardCompatibleComponent {
        @Configurable("swarm.deployment.*.context")
        @Configurable("swarm.http.context")
        public Defaultable<String> context = Defaultable.string("/");
    }
}
