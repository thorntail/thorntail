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

import java.util.function.Consumer;

import org.wildfly.swarm.config.MessagingActiveMQ;
import org.wildfly.swarm.config.messaging_activemq.ServerConsumer;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class MessagingFraction extends MessagingActiveMQ<MessagingFraction> implements Fraction {

    private MessagingFraction() {
    }

    public static MessagingFraction createDefaultFraction() {
        return new MessagingFraction().defaultServer();
    }

    public MessagingFraction defaultServer() {
        return defaultServer((s) -> {
            s.enableInVm();
        });
    }

    public MessagingFraction server(String childKey, EnhancedServerConsumer consumer) {
        return super.server( ()->{
            EnhancedServer s = new EnhancedServer(childKey);
            consumer.accept(s);
            return s;
        });
    }

    public MessagingFraction defaultServer(EnhancedServerConsumer config) {
        return server( "default", config );
    }
}
