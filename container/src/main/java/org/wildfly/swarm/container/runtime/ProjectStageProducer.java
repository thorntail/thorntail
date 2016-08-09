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
package org.wildfly.swarm.container.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.spi.api.ClassLoading;
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

    @Produces @Singleton
    public ProjectStage projectStage() {
        System.err.println( "*** producing projectStage" );
        try {
            String stageFile = System.getProperty(SwarmProperties.PROJECT_STAGE_FILE);
            System.err.println( "stage file prop: " + stageFile );

            URL url = null;

            if (stageFile != null) {
                url = new URL(stageFile);
                System.err.println( "from stage file: " + url );
            } else {
                try {
                    Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application" ) );
                    url = module.getClassLoader().getResource( "project-stages.yml" );
                    System.err.println( "from module: " + url );
                } catch (ModuleLoadException e) {
                    e.printStackTrace();
                }
            }

            if ( url == null ) {
                url = ClassLoader.getSystemClassLoader().getResource( "project-stages.yml" );
                System.err.println( "from classloader: " + url );
            }

            System.err.println( "  final: " + url );
            if ( url != null ) {
                return loadStageConfiguration(url);
            }
        } catch (MalformedURLException e) {
            System.err.println("[WARN] Failed to parse project stage URL reference, ignoring: " + e.getMessage());
        }

        return new ProjectStageImpl("default");
    }

    private ProjectStage loadStageConfiguration(URL url) {
        try {
            System.err.println( "Loading project stages from: " + url );
            InputStream in = url.openStream();
            System.err.println( "input stream: " + in );
            return enableStageConfiguration(in);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load stage configuration from URL :" + url.toExternalForm(), e);
        }
    }

    private ProjectStage enableStageConfiguration(InputStream input) {
        List<ProjectStage> projectStages = new ProjectStageFactory().loadStages(input);
        System.err.println( "ALL STAGES: " + projectStages );
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
