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

import org.jboss.as.domain.management.plugin.AuthenticationPlugIn;
import org.jboss.as.domain.management.plugin.AuthorizationPlugIn;
import org.jboss.as.domain.management.plugin.Credential;
import org.jboss.as.domain.management.plugin.PlugInProvider;
import org.wildfly.swarm.management.EnhancedSecurityRealm;

/**
 * @author Bob McWhirter
 */
public class InMemoryPlugInProvider implements PlugInProvider {


    @Override
    public AuthenticationPlugIn<Credential> loadAuthenticationPlugIn(String name) {
        if ( name.equals(EnhancedSecurityRealm.IN_MEMORY_PLUGIN_NAME ) ) {
            return new InMemoryAuthenticationPlugIn();
        }
        return null;
    }

    @Override
    public AuthorizationPlugIn loadAuthorizationPlugIn(String name) {
        if ( name.equals( EnhancedSecurityRealm.IN_MEMORY_PLUGIN_NAME ) ) {
            return new InMemoryAuthorizationPlugIn();
        }
        return null;
    }
}
