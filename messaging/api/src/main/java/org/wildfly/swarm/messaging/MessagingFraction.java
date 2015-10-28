/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.messaging;

import java.util.Arrays;

import org.wildfly.swarm.config.MessagingActiveMQ;
import org.wildfly.swarm.config.messaging_activemq.server.ConnectionFactory;
import org.wildfly.swarm.config.messaging_activemq.server.PooledConnectionFactory;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class MessagingFraction extends MessagingActiveMQ<MessagingFraction> implements Fraction {

    private MessagingFraction() {
    }

    public static MessagingFraction createDefaultFraction() {
        return new MessagingFraction();
    }

    public MessagingFraction server(String childKey, ServerConfigurator config) {
        Server s = new Server(childKey);
        config.configure(s);
        return server(s);
    }

    public MessagingFraction defaultServer() {
        return defaultServer( (s)->{} );
    }

    public MessagingFraction defaultServer(ServerConfigurator config) {
        server("default", (s) -> {
            s.enableInVm();
            config.configure(s);
        });
        return this;
    }
}
