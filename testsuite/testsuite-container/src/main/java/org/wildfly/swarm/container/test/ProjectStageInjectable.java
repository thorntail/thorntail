package org.wildfly.swarm.container.test;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Bob McWhirter
 */
@Pre
@Singleton
public class ProjectStageInjectable implements Customizer {

    @Inject
    private Instance<ProjectStage> stage;

    @Override
    public void customize() {
        if (this.stage.isUnsatisfied()) {
            throw new AssertionError("project stages not present");
        }
    }
}
