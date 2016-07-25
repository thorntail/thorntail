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

import org.wildfly.swarm.config.Mail;
import org.wildfly.swarm.config.mail.MailSession;
import org.wildfly.swarm.config.mail.mail_session.SMTPServer;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.annotations.Default;
import org.wildfly.swarm.spi.api.annotations.ExtensionModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;

/**
 * @author Ken Finnigan
 */
@ExtensionModule("org.jboss.as.mail")
@MarshalDMR
public class MailFraction extends Mail<MailFraction> implements Fraction {

    public MailFraction() {
    }

    @Default
    public static MailFraction defaultFraction() {
        return new MailFraction()
                .mailSession("Default", (session) -> {
                    session.smtpServer((server) -> {
                        server.host("localhost");
                        server.port("25");
                    });
                });
    }

    public MailFraction mailSession(String key, EnhancedMailSessionConsumer consumer) {
        EnhancedMailSession session = new EnhancedMailSession(key);
        return super.mailSession(() -> {
            if (consumer != null) {
                consumer.accept(session);
                if (session.jndiName() == null) {
                    session.jndiName("java:jboss/mail/" + key);
                }
            }
            return session;
        });
    }

    public MailFraction smtpServer(String key, EnhancedSMTPServerConsumer consumer) {
        return this.mailSession(key, (session) -> {
            session.smtpServer(consumer);
        });
    }

    @Override
    public void postInitialize(Fraction.PostInitContext initContext) {
        for (MailSession session : subresources().mailSessions()) {
            SMTPServer server = session.subresources().smtpServer();
            if (server != null && server instanceof EnhancedSMTPServer) {
                OutboundSocketBinding socketBinding = ((EnhancedSMTPServer) server).outboundSocketBinding();
                if (socketBinding != null) {
                    initContext.outboundSocketBinding(socketBinding);
                }
            }
        }
    }
}
