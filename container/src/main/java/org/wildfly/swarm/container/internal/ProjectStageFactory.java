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
package org.wildfly.swarm.container.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.Vetoed;

import org.wildfly.swarm.spi.api.ProjectStage;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Heiko Braun
 * @since 07/04/16
 */
@Vetoed
public class ProjectStageFactory {

    public List<ProjectStage> loadStages(InputStream inputStream) {

        try {
            List<ProjectStage> stages = new LinkedList<>();
            Yaml yaml = new Yaml();
            Iterable<Object> docs = yaml.loadAll(inputStream);

            for (Object item : docs) {
                Map<String, Map<String, String>> doc = (Map<String, Map<String, String>>) item;

                String stageName = doc.get(PROJECT_PREFIX) != null ? doc.get(PROJECT_PREFIX).get(STAGE) : DEFAULT;
                ProjectStageImpl stage = new ProjectStageImpl(stageName);

                for (String key : doc.keySet()) {
                    if (!key.equals(PROJECT_PREFIX)) {
                        stage.getConfig().put(key, doc.get(key));
                    }
                }

                stages.add(stage.initialize());

            }


            Optional<ProjectStage> defaultStage = stages.stream()
                    .filter(stage -> DEFAULT.equals(stage.getName()))
                    .findFirst();

            if(!defaultStage.isPresent())
                throw new RuntimeException("Missing stage 'default' in project-stages.yml");

            // inherit values from default stage
            final Map<String, String> defaults = defaultStage.get().getProperties();
            stages.stream()
                    .filter(stage -> !stage.getName().equals(DEFAULT))
                    .forEach(stage -> {
                        Map<String, String> current = stage.getProperties();
                        Set<String> currentKeys = current.keySet();
                        defaults.keySet().forEach(
                                key -> {
                                    if(!currentKeys.contains(key))
                                        current.put(key, defaults.get(key));
                                }
                        );
                    });

            return stages;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                //
            }
        }
    }

    private static final String PROJECT_PREFIX = "project";

    private static final String STAGE = "stage";

    private static final String DEFAULT = "default";
}
