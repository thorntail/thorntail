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
package org.wildfly.swarm.management.runtime.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.domain.management.plugin.AuthorizationPlugIn;
import org.jboss.as.domain.management.plugin.PlugInConfigurationSupport;

/**
 * @author Bob McWhirter
 */
public class InMemoryAuthorizationPlugIn implements AuthorizationPlugIn, PlugInConfigurationSupport {

    @Override
    public void init(Map<String, String> configuration, Map<String, Object> sharedState) throws IOException {

        for (String key : configuration.keySet()) {
            if (key.endsWith(".roles")) {
                String userName = key.substring(0, key.length() - ".roles".length());
                String value = configuration.get(key);

                String[] roles = value.split(",");

                for (int i = 0; i < roles.length; ++i) {
                    roles[i] = roles[i].trim();
                }

                this.roles.put(userName, roles);
            }
        }
    }

    @Override
    public String[] loadRoles(String userName, String realm) throws IOException {
        if (!this.roles.containsKey(userName)) {
            return new String[0];
        }

        return this.roles.get(userName);
    }

    private Map<String, String[]> roles = new HashMap<>();

}
