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
import org.wildfly.swarm.container.OutboundSocketBinding;

/**
 * @author Bob McWhirter
 */
public class EnhancedSMTPServer extends SMTPServer<EnhancedSMTPServer> {

    public EnhancedSMTPServer(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public EnhancedSMTPServer host(String host) {
        this.host = host;
        return this;
    }

    public EnhancedSMTPServer port(String port) {
        this.port = port;
        return this;
    }

    public EnhancedSMTPServer port(int port) {
        this.port = "" + port;
        return this;
    }

    public OutboundSocketBinding outboundSocketBinding() {
        if (this.host == null && this.port == null) {
            return null;
        }

        this.outboundSocketBindingRef("mail-smtp-" + this.sessionKey);

        return new OutboundSocketBinding("mail-smtp-" + this.sessionKey)
                .remoteHost(this.host)
                .remotePort(this.port);
    }

    private final String sessionKey;

    private String host;

    private String port;


}
