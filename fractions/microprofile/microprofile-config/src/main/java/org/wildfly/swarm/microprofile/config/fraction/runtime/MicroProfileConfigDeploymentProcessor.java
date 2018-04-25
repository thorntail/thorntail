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
package org.wildfly.swarm.microprofile.config.fraction.runtime;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.api.config.CompositeKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 4/24/18
 */
@DeploymentScoped
@SuppressWarnings("rawtypes")
public class MicroProfileConfigDeploymentProcessor implements DeploymentProcessor {
    private static final String CONFIG_SOURCE_SERVICES = "META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource";
    private static Logger log = Logger.getLogger(MicroProfileConfigDeploymentProcessor.class);

    private final Archive archive;
    private final ConfigView configView;

    @Inject
    public MicroProfileConfigDeploymentProcessor(Archive archive, ConfigView configView) {
        this.archive = archive;
        this.configView = configView;
    }

    @Override
    public void process() {
        CompositeKey key = new CompositeKey("swarm", "microprofile", "config", "config-sources");

        List<String> configSourceClasses = readConfigSourceClasses(key);

        defineServices(configSourceClasses);

        log.debugf("Configured MicroProfile config source classes %s", configSourceClasses);
    }

    private void defineServices(List<String> configSourceClasses) {
        StringBuilder servicesContent = new StringBuilder();
        servicesContent.append(readCurrentServices());
        configSourceClasses
                .forEach(source -> servicesContent.append(source).append("\n"));

        archive.add(new StringAsset(servicesContent.toString()), new BasicPath(CONFIG_SOURCE_SERVICES));
    }

    private String readCurrentServices()  {
        Node node = archive.get(CONFIG_SOURCE_SERVICES);
        if (node != null) {
            try (InputStream stream = node.getAsset().openStream();
                 InputStreamReader streamReader = new InputStreamReader(stream);
                 BufferedReader reader = new BufferedReader(streamReader)
            ) {
                return reader.lines()
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.joining("\n"));
            } catch (IOException e) {
                throw new IllegalStateException("Unable to parse " + CONFIG_SOURCE_SERVICES + " file", e);
            }
        }
        return "";
    }

    private List<String> readConfigSourceClasses(CompositeKey key) {
        return configView.simpleSubkeys(key)
                    .stream()
                    .map(subKey -> key.append(subKey).append("attribute-class"))
                    .map(configView::valueOf)
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
    }
}
