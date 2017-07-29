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
package org.wildfly.swarm.container.runtime.deployments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.DefaultDeploymentFactory;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class DefaultDeploymentCreator {

    private Map<String, DefaultDeploymentFactory> factories = new HashMap<>();

    @Inject
    public DefaultDeploymentCreator(@Any Instance<DefaultDeploymentFactory> factories) {
        this((Iterable<DefaultDeploymentFactory>) factories);
    }

    public DefaultDeploymentCreator(DefaultDeploymentFactory... factories) {
        this(Arrays.asList(factories));
    }

    public DefaultDeploymentCreator(Iterable<DefaultDeploymentFactory> factories) {
        for (DefaultDeploymentFactory factory : factories) {
            final DefaultDeploymentFactory current = this.factories.get(factory.getType());
            if (current == null) {
                this.factories.put(factory.getType(), factory);
            } else {
                // if this one is high priority than the previously-seen factory, replace it.
                if (factory.getPriority() > current.getPriority()) {
                    this.factories.put(factory.getType(), factory);
                }
            }
        }
    }

    /** @return {@code null} if there's no {@link DefaultDeploymentFactory} for given deployment {@code type} */
    public Archive<?> createDefaultDeployment(String type) {
        DefaultDeploymentFactory factory = getFactory(type);
        if (factory == null) {
            return null;
        }

        try {
            return factory.create();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    DefaultDeploymentFactory getFactory(String type) {
        return this.factories.get(type);
    }
}
