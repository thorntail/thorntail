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
package org.wildfly.swarm.management;

import org.wildfly.swarm.config.management.SecurityRealm;
import org.wildfly.swarm.config.management.SecurityRealmConsumer;

/**
 * @author Bob McWhirter
 */
public class EnhancedSecurityRealm extends SecurityRealm<EnhancedSecurityRealm> {

    public static final String IN_MEMORY_PLUGIN_NAME = "swarm-in-memory";

    public EnhancedSecurityRealm(String key) {
        super(key);
        plugIn("org.wildfly.swarm.management:runtime");
    }

    public EnhancedSecurityRealm inMemoryAuthentication(InMemoryAuthentication.Consumer consumer) {
        return plugInAuthentication((plugin) -> {
            plugin.name(IN_MEMORY_PLUGIN_NAME);
            InMemoryAuthentication authn = new InMemoryAuthentication(getKey(), plugin);
            consumer.accept(authn);
        });
    }

    public EnhancedSecurityRealm inMemoryAuthorization() {
        return inMemoryAuthorization((authz) -> {
        });
    }

    public EnhancedSecurityRealm inMemoryAuthorization(InMemoryAuthorization.Consumer consumer) {
        return plugInAuthorization((plugin) -> {
            plugin.name(IN_MEMORY_PLUGIN_NAME);
            InMemoryAuthorization authz = new InMemoryAuthorization(plugin);
            consumer.accept(authz);
        });
    }

    public static interface Consumer extends SecurityRealmConsumer<EnhancedSecurityRealm> {

    }

}
