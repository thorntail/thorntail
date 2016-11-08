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
package org.wildfly.swarm.mail;

import org.wildfly.swarm.config.mail.mail_session.SMTPServer;
import org.wildfly.swarm.spi.api.Configurable;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;

import static org.wildfly.swarm.spi.api.Configurable.integer;
import static org.wildfly.swarm.spi.api.Configurable.string;

/**
 * @author Bob McWhirter
 */
public class EnhancedSMTPServer extends SMTPServer<EnhancedSMTPServer> {

    public EnhancedSMTPServer(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String sessionKey() {
        return this.sessionKey;
    }

    public EnhancedSMTPServer host(String host) {
        this.host.set( host );
        return this;
    }

    public String host() {
        return this.host.get();
    }

    public EnhancedSMTPServer port(int port) {
        this.port.set( port );
        return this;
    }

    public int port() {
        return this.port.get();
    }

    /*
    public OutboundSocketBinding outboundSocketBinding() {
        if (this.host == null && this.port == null) {
            return null;
        }

        this.outboundSocketBindingRef("mail-smtp-" + this.sessionKey);

        return new OutboundSocketBinding("mail-smtp-" + this.sessionKey)
                .remoteHost(this.host)
                .remotePort(this.port);
    }
    */

    private final String sessionKey;

    private Configurable<String> host = string( "swarm.mail.smtp.host", "localhost");

    private Configurable<Integer> port = integer( "swarm.mail.smtp.port", 25 );


}
