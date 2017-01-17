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
import java.util.Map;

import javax.enterprise.inject.Vetoed;

import org.yaml.snakeyaml.Yaml;

/**
 * @author Heiko Braun
 * @since 07/04/16
 */
@Vetoed
public class ConfigViewFactory {

    private final ConfigViewImpl configView;

    public ConfigViewFactory() {
        this.configView = new ConfigViewImpl();
    }

    public ConfigViewImpl build() {
        return this.configView;
    }

    public void loadProjectStages(URL url) throws IOException {
        loadProjectStages(url.openStream());
    }

    public void loadProjectConfig(String name, URL url) throws IOException {
        loadProjectConfig(name, url.openStream());
    }

    public void loadProjectStages(InputStream inputStream) {
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

    public void loadProjectConfig(String name, InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, ?> doc = (Map<String, ?>) yaml.load(inputStream);

        ConfigNode node = MapConfigNodeFactory.load(doc);
        this.configView.register(name, node);
    }

    private static final String PROJECT_PREFIX = "project";

    private static final String STAGE = "stage";

    private static final String DEFAULT = "default";
}
