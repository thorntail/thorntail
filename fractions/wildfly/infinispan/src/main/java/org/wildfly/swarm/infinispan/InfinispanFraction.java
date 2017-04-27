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
package org.wildfly.swarm.infinispan;

import org.wildfly.swarm.config.Infinispan;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Lance Ball
 * @author Toby Crawley
 */
@MarshalDMR
@WildFlyExtension(module = "org.jboss.as.clustering.infinispan")
@DeploymentModule(name = "org.infinispan", export = true)
@DeploymentModule(name = "org.infinispan.commons", export = true)
public class InfinispanFraction extends Infinispan<InfinispanFraction> implements Fraction<InfinispanFraction> {

    public InfinispanFraction() {
    }

    @Override
    public InfinispanFraction applyDefaults() {
        markDefaultFraction();
        return this;
    }

    public static InfinispanFraction createDefaultFraction() {
        return new InfinispanFraction().markDefaultFraction();
    }

    protected InfinispanFraction markDefaultFraction() {
        this.defaultFraction = true;

        return this;
    }

    public boolean isDefaultFraction() {
        return this.defaultFraction;
    }

    private boolean defaultFraction = false;
}
