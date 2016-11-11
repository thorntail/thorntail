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
package org.wildfly.swarm.mail.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.config.mail.MailSession;
import org.wildfly.swarm.config.mail.mail_session.SMTPServer;
import org.wildfly.swarm.mail.EnhancedSMTPServer;
import org.wildfly.swarm.mail.MailFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Ken Finnigan
 */
@Post
@ApplicationScoped
public class SocketBindingCustomizer implements Customizer {
    @Inject
    @Any
    private Instance<MailFraction> mailInstance;

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    @Override
    public void customize() {
        if (!mailInstance.isUnsatisfied()) {
            MailFraction mailFraction = mailInstance.get();

            for (MailSession session : mailFraction.subresources().mailSessions()) {
                SMTPServer server = session.subresources().smtpServer();
                if (server != null && server instanceof EnhancedSMTPServer) {
                    if (server.outboundSocketBindingRef() == null) {
                        String ref = "mail-smtp-" + ((EnhancedSMTPServer) server).sessionKey();
                        this.group.outboundSocketBinding(
                                new OutboundSocketBinding(ref)
                                        .remoteHost(((EnhancedSMTPServer) server).host())
                                        .remotePort(((EnhancedSMTPServer) server).port()));

                        ((EnhancedSMTPServer) server).outboundSocketBindingRef(ref);
                    }
                }
            }
        }
    }
}
