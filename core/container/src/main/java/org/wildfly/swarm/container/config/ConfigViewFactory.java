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
package org.wildfly.swarm.container.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.enterprise.inject.Vetoed;

import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.spi.api.ConfigurationFilter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 * @author Heiko Braun
 * @since 07/04/16
 */
@Vetoed
public class ConfigViewFactory {

    private static final String STAGES = "stages";

    private final ConfigViewImpl configView;

    public static ConfigViewFactory defaultFactory() throws ModuleLoadException {
        return defaultFactory(null, System.getenv());
    }

    public static ConfigViewFactory defaultFactory(Properties properties, Map<String, String> environment) throws ModuleLoadException {
        return new ConfigViewFactory(
                properties,
                environment,
                new FilesystemConfigLocator(),
                ClassLoaderConfigLocator.system(),
                ClassLoaderConfigLocator.forApplication()
        );
    }

    public ConfigViewFactory(Properties properties) {
        this.configView = new ConfigViewImpl().withProperties(properties).withEnvironment(System.getenv());
    }

    public ConfigViewFactory(Properties properties, Map<String, String> environment) {
        this.configView = new ConfigViewImpl().withProperties(properties).withEnvironment(environment);
    }

    public ConfigViewFactory(Properties properties, Map<String, String> environment, ConfigLocator... locators) {
        this(properties, environment);
        for (ConfigLocator locator : locators) {
            addLocator(locator);
        }
    }

    public void addLocator(ConfigLocator locator) {
        this.locators.add(locator);
    }

    public ConfigViewFactory load(String profileName) {
        this.profiles.add(profileName);
        this.locators
                .stream()
                .flatMap(locator -> {
                    try {
                        return locator.locate(profileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .forEach(url -> {
                    try {
                        load(profileName, url);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        return this;
    }

    public void load(String profileName, URL url) throws IOException {
        if (url.getPath().endsWith(".properties")) {
            loadProperties(profileName, url);
        } else if (url.getPath().endsWith(".yml") || url.getPath().endsWith(".yaml")) {
            loadYaml(profileName, url);
        }
    }

    protected void loadProperties(String profileName, URL url) throws IOException {
        Properties props = new Properties();
        props.load(url.openStream());
        ConfigNode configNode = PropertiesConfigNodeFactory.load(props);
        this.configView.register(profileName, configNode);
        this.configView.withProfile(profileName);
    }

    protected void loadYaml(String profileName, URL url) throws IOException {
        if (profileName.equals(STAGES) || url.getPath().endsWith("-stages.yml") || url.getPath().endsWith("-stages.yaml")) {
            loadProjectStages(url);
            return;
        }

        loadYamlProjectConfig(profileName, url);
    }

    public ConfigViewImpl get() {
        return this.configView;
    }

    public ConfigViewImpl get(boolean activate) {
        for (String profile : this.profiles) {
            this.configView.withProfile(profile);
        }

        this.configView.activate();
        return this.configView;
    }

    private void loadProjectStages(URL url) throws IOException {
        loadProjectStages(url.openStream());
    }

    @SuppressWarnings("unchecked")
    private void loadProjectStages(InputStream inputStream) {
        Yaml yaml = newYaml(System.getenv());
        Iterable<Object> docs = yaml.loadAll(inputStream);

        for (Object item : docs) {
            Map<String, Map<String, String>> doc = (Map<String, Map<String, String>>) item;

            String name = DEFAULT;
            if (doc.get(PROJECT_PREFIX) != null) {
                name = doc.get(PROJECT_PREFIX).get(STAGE);
                doc.remove(PROJECT_PREFIX);
            }
            ConfigNode node = MapConfigNodeFactory.load(doc);

            if (name.equals(DEFAULT)) {
                this.configView.withDefaults(node);
            } else {
                this.configView.register(name, node);
            }
        }
    }

    private void loadYamlProjectConfig(String name, URL url) throws IOException {
        loadYamlProjectConfig(name, url.openStream());
    }

    @SuppressWarnings("unchecked")
    private void loadYamlProjectConfig(String name, InputStream inputStream) {
        Map<String, ?> doc = loadYaml(inputStream);

        ConfigNode node = MapConfigNodeFactory.load(doc);
        this.configView.register(name, node);
        this.configView.withProfile(name);
    }

    static Map<String, ?> loadYaml(InputStream input, Map<String, String> environment) {
        Yaml yaml = newYaml(environment);
        return (Map<String, ?>) yaml.load(input);
    }

    static Map<String, ?> loadYaml(InputStream input) {
        return loadYaml(input, System.getenv());
    }

    private static Yaml newYaml(Map<String, String> environment) {
        return new Yaml(new EnvironmentConstructor(environment),
                        new Representer(),
                        new DumperOptions(),
                        new Resolver() {
                            @Override
                            public Tag resolve(NodeId kind, String value, boolean implicit) {
                                if (value != null) {
                                    if (value.startsWith("${env.")) {
                                        return new Tag("!env");
                                    }
                                    if (value.equalsIgnoreCase("on") ||
                                            value.equalsIgnoreCase("off") ||
                                            value.equalsIgnoreCase("yes") ||
                                            value.equalsIgnoreCase("no")) {
                                        return Tag.STR;
                                    }
                                }
                                return super.resolve(kind, value, implicit);
                            }
                        });
    }


    public void withProfile(String name) {
        this.configView.withProfile(name);
    }

    public void withProperty(String name, String value) {
        this.configView.withProperty(name, value);
    }

    public void withFilter(ConfigurationFilter filter) {
        this.configView.withFilter(filter);
    }


    private List<ConfigLocator> locators = new ArrayList<>();

    private List<String> profiles = new ArrayList<>();

    private static final String PROJECT_PREFIX = "project";

    private static final String STAGE = "stage";

    private static final String DEFAULT = "default";

}
