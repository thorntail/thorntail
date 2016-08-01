package org.wildfly.swarm.container.runtime.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.wildfly.swarm.container.internal.ProjectStageFactory;
import org.wildfly.swarm.container.internal.ProjectStageImpl;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Bob McWhirter
 */
@Singleton
public class ProjectStageProducer {

    public ProjectStageProducer() {
        System.err.println( "*** ctor ProjectStageProducer" );
    }

    @Produces @Dependent
    public StageConfig stageConfig() {
        System.err.println( "*** producing stageConfig" );
        return new StageConfig( projectStage() );
    }

    @Produces @Dependent
    public ProjectStage projectStage() {
        System.err.println( "*** producing projectStage" );
        try {
            String stageFile = System.getProperty(SwarmProperties.PROJECT_STAGE_FILE);
            if (stageFile != null) {
                return loadStageConfiguration(new URL(stageFile));
            }

        } catch (MalformedURLException e) {
            System.err.println("[WARN] Failed to parse project stage URL reference, ignoring: " + e.getMessage());
        }

        return new ProjectStageImpl("default");
    }

    private ProjectStage loadStageConfiguration(URL url) {
        try {
            return enableStageConfiguration(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load stage configuration from URL :" + url.toExternalForm(), e);
        }
    }

    private ProjectStage enableStageConfiguration(InputStream input) {
        List<ProjectStage> projectStages = new ProjectStageFactory().loadStages(input);
        String stageName = System.getProperty(SwarmProperties.PROJECT_STAGE, "default");
        ProjectStage stage = null;
        for (ProjectStage projectStage : projectStages) {
            if (projectStage.getName().equals(stageName)) {
                stage = projectStage;
                break;
            }
        }

        if (null == stage) {
            throw new RuntimeException("Project stage '" + stageName + "' cannot be found");
        }

        System.out.println("[INFO] Using project stage: " + stageName);

        return stage;
    }
}
