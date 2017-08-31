/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.container.runtime.usage;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.wildfly.swarm.container.runtime.ConfigurableHandle;
import org.wildfly.swarm.container.runtime.ConfigurableManager;

/**
 * Created by bob on 8/30/17.
 */
@ApplicationScoped
public class ConfigurableManagerUsageVariableSupplier implements UsageVariableSupplier {

    @Inject
    public ConfigurableManagerUsageVariableSupplier(ConfigurableManager manager) {
        this.manager = manager;
    }

    @Override
    public Object valueOf(String name) throws Exception {
        Optional<ConfigurableHandle> configurable = this.manager.configurables().stream()
                .filter(e -> e.key().toString().equals(name))
                .findFirst();

        if (configurable.isPresent()) {
            return configurable.get().currentValue();
        }

        return null;
    }

    private final ConfigurableManager manager;
}
