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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractServerConfiguration<T extends Fraction> implements ServerConfiguration<T> {

    private final Class<T> type;

    private List<DeploymentSpec> deployments = new ArrayList<>();

    public AbstractServerConfiguration(Class<T> type) {
        this.type = type;
    }

    protected DeploymentSpec deployment(String gav) {
        DeploymentSpec spec = new DeploymentSpec(gav);
        this.deployments.add(spec);
        return spec;
    }

    @Override
    public List<Archive> getImplicitDeployments(T fraction) throws Exception {
        if (this.deployments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Archive> archives = new ArrayList<>();

        for (DeploymentSpec deployment : this.deployments) {
            archives.add(deployment.toArchive(fraction));
        }

        return archives;
    }

    @Override
    public Class<T> getType() {
        return this.type;
    }

    @Override
    public T defaultFraction() {
        try {
            return getType().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public class DeploymentSpec {
        private final String gav;

        private String asName;

        private BiConsumer<T, Archive<?>> config;

        public DeploymentSpec(String gav) {
            this.gav = gav;
        }

        public DeploymentSpec as(String asName) {
            this.asName = asName;
            return this;
        }

        public DeploymentSpec configure(BiConsumer<T, Archive<?>> config) {
            this.config = config;
            return this;
        }

        Archive<?> toArchive(T fraction) throws Exception {
            Archive<?> archive;

            if (this.asName == null) {
                archive = Swarm.artifact(this.gav);
            } else {
                archive = Swarm.artifact(this.gav, asName);
            }

            if (this.config != null) {
                this.config.accept(fraction, archive);
            }

            return archive;
        }

    }

}
