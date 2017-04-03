/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.container.config;

import org.jboss.modules.ModuleLoadException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import javax.enterprise.inject.Vetoed;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

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
        } else if (url.getPath().endsWith(".yml")) {
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
        if (profileName.equals(STAGES) || url.getPath().endsWith("-stages.yml")) {
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
        Yaml yaml = newYaml();
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

    static Map<String, ?> loadYaml(InputStream input) {
        Yaml yaml = newYaml();

        yaml.addImplicitResolver(Tag.STR, Pattern.compile("on"), null);
        return (Map<String, ?>) yaml.load(input);
    }

    private static Yaml newYaml() {
        return new Yaml(new Constructor(),
                        new Representer(),
                        new DumperOptions(),
                        new Resolver() {
                            @Override
                            public Tag resolve(NodeId kind, String value, boolean implicit) {
                                if (value != null) {
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

    private List<ConfigLocator> locators = new ArrayList<>();

    private List<String> profiles = new ArrayList<>();

    private static final String PROJECT_PREFIX = "project";

    private static final String STAGE = "stage";

    private static final String DEFAULT = "default";

}
