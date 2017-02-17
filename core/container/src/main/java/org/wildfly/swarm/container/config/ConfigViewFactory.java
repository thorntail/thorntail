/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
import org.yaml.snakeyaml.Yaml;

/**
 * @author Heiko Braun
 * @since 07/04/16
 */
@Vetoed
public class ConfigViewFactory {

    private static final String STAGES = "stages";

    private final ConfigViewImpl configView;

    public static ConfigViewFactory defaultFactory() throws ModuleLoadException {
        return defaultFactory(null);
    }

    public static ConfigViewFactory defaultFactory(Properties properties) throws ModuleLoadException {
        return new ConfigViewFactory(
                properties,
                new FilesystemConfigLocator(),
                ClassLoaderConfigLocator.system(),
                ClassLoaderConfigLocator.forApplication()
        );
    }

    private ConfigViewFactory(Properties properties) {
        this.configView = new ConfigViewImpl().withProperties(properties);
    }

    public ConfigViewFactory(Properties properties, ConfigLocator... locators) {
        this(properties);
        for (ConfigLocator locator : locators) {
            addLocator(locator);
        }
    }

    public void addLocator(ConfigLocator locator) {
        this.locators.add(locator);
    }

    public ConfigViewFactory load(String profileName) {
        this.locators
                .stream()
                .flatMap(locator -> {
                    try {
                        return locator.locate(profileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(url -> {
                    try {
                        load(profileName, url);
                    } catch (IOException e) {
                        e.printStackTrace();
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
    }

    protected void loadYaml(String profileName, URL url) throws IOException {
        if (profileName.equals(STAGES) || url.getPath().endsWith("-stages.yml")) {
            loadProjectStages(url);
            return;
        }

        loadYamlProjectConfig(profileName, url);
    }

    public ConfigViewImpl build() {
        return this.configView;
    }

    private void loadProjectStages(URL url) throws IOException {
        loadProjectStages(url.openStream());
    }

    private void loadProjectStages(InputStream inputStream) {
        Yaml yaml = new Yaml();
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

    private void loadYamlProjectConfig(String name, InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, ?> doc = (Map<String, ?>) yaml.load(inputStream);

        ConfigNode node = MapConfigNodeFactory.load(doc);
        this.configView.register(name, node);
    }

    private List<ConfigLocator> locators = new ArrayList<>();

    private static final String PROJECT_PREFIX = "project";

    private static final String STAGE = "stage";

    private static final String DEFAULT = "default";
}
