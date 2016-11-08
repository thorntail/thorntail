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
package org.wildfly.swarm.container.runtime.cdi;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.literal.DefaultLiteral;
import org.wildfly.swarm.container.cdi.ProjectStageImpl;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.StageConfig;

/** Produces an explicitly set project-stage.
 *
 * @author Bob McWhirter
 */
public class ProjectStageProducingExtension implements Extension {

    private final Optional<ProjectStage> projectStage;

    public ProjectStageProducingExtension(Optional<ProjectStage> projectStage) {
        this.projectStage = projectStage;
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {
        abd.addBean().addType( ProjectStage.class )
                .scope(Dependent.class)
                .qualifiers( DefaultLiteral.INSTANCE )
                .produceWith(this::getProjectStage);

        abd.addBean().addType(StageConfig.class)
                .scope(Dependent.class)
                .qualifiers(DefaultLiteral.INSTANCE)
                .produceWith(this::getStageConfig);
    }

    protected ProjectStage getProjectStage() {
        if (this.projectStage.isPresent()) {
            return this.projectStage.get();
        }

        return new ProjectStageImpl("default");
    }

    protected StageConfig getStageConfig() {
        return new StageConfig( getProjectStage() );
    }
}
