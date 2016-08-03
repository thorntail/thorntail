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
package org.wildfly.swarm.remoting;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.server.HTTPListener;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Post;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Ken Finnigan
 */
@Post
@ApplicationScoped
public class EnableListener implements Customizer {

    @Inject
    @Any
    Instance<Undertow> undertow;

    @Override
    public void customize() {
        System.setProperty(SwarmProperties.HTTP_EAGER, "true");

        if (!undertow.isUnsatisfied()) {
            Server server = undertow.get().subresources().server("default-server");
            if (server != null) {
                HTTPListener listener = server.subresources().httpListener("default");
                if (listener != null) {
                    listener.enabled(true);
                }
            }
        }
    }
}
