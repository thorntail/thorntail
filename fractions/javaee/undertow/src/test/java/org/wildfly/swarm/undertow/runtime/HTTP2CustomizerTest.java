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
package org.wildfly.swarm.undertow.runtime;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.server.HttpsListener;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.UndertowProperties;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class HTTP2CustomizerTest {

    @Test
    public void testHTTP2Enabled() {

        HTTP2Customizer customizer = new HTTP2Customizer();
        customizer.undertow = new UndertowFraction().applyDefaults();
        Server server = customizer.undertow.subresources().server(UndertowProperties.DEFAULT_SERVER);

        AtomicReference<HttpsListener> listener = new AtomicReference<>();
        server.httpsListener("default-https", (config) -> {
            listener.set(config);
        });

        assertThat(listener.get()).isNotNull();

        assertThat(listener.get().enableHttp2()).isNull();
        customizer.customize();
        assertThat(listener.get().enableHttp2()).isTrue();

    }
}
