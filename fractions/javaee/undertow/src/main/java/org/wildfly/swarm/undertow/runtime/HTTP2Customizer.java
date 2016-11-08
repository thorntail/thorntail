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
package org.wildfly.swarm.undertow.runtime;


import javax.inject.Inject;

import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * A {@link Customizer} implementation to enable HTTP/2
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Post
public class HTTP2Customizer implements Customizer {

    @Inject
    private UndertowFraction undertow;

    @Override
    public void customize() {
        for (Server server : undertow.subresources().servers()) {
            server.subresources().httpsListeners().forEach(httpsListener -> httpsListener.enableHttp2(true));
        }
    }
}
