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

import org.jboss.as.domain.management.plugin.AuthenticationPlugIn;
import org.jboss.as.domain.management.plugin.Credential;
import org.jboss.as.domain.management.plugin.DigestCredential;
import org.jboss.as.domain.management.plugin.Identity;
import org.jboss.as.domain.management.plugin.PlugInConfigurationSupport;

/**
 * @author Bob McWhirter
 */
public class InMemoryAuthenticationPlugIn implements AuthenticationPlugIn<Credential>, PlugInConfigurationSupport {

    @Override
    public void init(Map<String, String> configuration, Map<String, Object> sharedState) throws IOException {
        for (String key : configuration.keySet()) {
            if (key.endsWith(".hash")) {
                String userName = key.substring(0, key.length() - ".hash".length());
                String hash = configuration.get(key);
                this.credentials.put(userName, hash);
            }
        }
    }

    @Override
    public Identity<Credential> loadIdentity(String userName, String realm) throws IOException {

        if (!this.credentials.containsKey(userName)) {
            return null;
        }

        String hash = this.credentials.get(userName);
        return new Identity<Credential>() {
            @Override
            public String getUserName() {
                return userName;
            }

            @Override
            public Credential getCredential() {
                return new DigestCredential(hash);
            }
        };
    }

    private Map<String, String> credentials = new HashMap<>();

}
