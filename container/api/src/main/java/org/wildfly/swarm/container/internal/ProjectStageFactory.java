package org.wildfly.swarm.container.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.wildfly.swarm.spi.api.ProjectStage;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Heiko Braun
 * @since 07/04/16
 */
public class ProjectStageFactory {

    public List<ProjectStage> loadStages(InputStream inputStream) {

        try {
            List<ProjectStage> stages = new LinkedList<>();
            Yaml yaml = new Yaml();
            Iterable<Object> docs = yaml.loadAll(inputStream);

            for (Object item : docs) {
                Map<String,Map<String,String>> doc = (Map<String,Map<String,String>>)item;

                String stageName = doc.get(PROJECT_PREFIX)!=null ? doc.get(PROJECT_PREFIX).get(STAGE) : DEFAULT;
                ProjectStageImpl stage = new ProjectStageImpl(stageName);

                for (String key : doc.keySet()) {
                    if(!key.equals(PROJECT_PREFIX)) {
                        stage.getConfig().put(key, doc.get(key));
                    }
                }

                stages.add(stage.initialize());

            }

            return stages;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                //
            }
        }
    };

    private static final String PROJECT_PREFIX = "project";

    private static final String STAGE = "stage";

    private static final String DEFAULT = "default";
}
