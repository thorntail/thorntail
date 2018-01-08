/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.microprofile.openapi.runtime;

import javax.inject.Inject;

import org.jboss.jandex.IndexView;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("rawtypes")
@DeploymentScoped
public class OpenApiDeploymentProcessor implements DeploymentProcessor {

    private final Archive archive;
    private final IndexView index;

    /**
     * Constructor.
     * @param archive
     * @param index
     */
    @Inject
    public OpenApiDeploymentProcessor(Archive archive, IndexView index) {
        this.archive = archive;
        this.index = index;
        System.out.println("===");
        System.out.println("Creating instance of: " + getClass().getName());
        System.out.println("===");
    }

    /**
     * @see org.wildfly.swarm.spi.api.DeploymentProcessor#process()
     */
    @Override
    public void process() throws Exception {
        System.out.println("----");
        System.out.println("Initializing OpenApi support!!");
        System.out.println("----");
    }

}
