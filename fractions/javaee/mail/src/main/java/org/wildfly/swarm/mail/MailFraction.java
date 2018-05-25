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
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Ken Finnigan
 */
@WildFlyExtension(module = "org.jboss.as.mail")
@MarshalDMR
public class MailFraction extends Mail<MailFraction> implements Fraction<MailFraction> {

    public static MailFraction defaultFraction() {
        return new MailFraction().applyDefaults();
    }

    public MailFraction applyDefaults() {
        mailSession("default", EnhancedMailSession::smtpServer);
        return this;
    }

    @Configurable
    public MailFraction mailSession(String key, EnhancedMailSessionConsumer consumer) {
        @SuppressWarnings("rawtypes")
        MailSession mailSession = subresources().mailSession(key);
        if (mailSession == null) {
            // No mail session exists yet
            EnhancedMailSession enhancedMailSession = new EnhancedMailSession(key);
            applyConsumer(enhancedMailSession, consumer, key);
            return super.mailSession(enhancedMailSession);
        } else {
            if (!(mailSession instanceof EnhancedMailSession)) {
                throw new IllegalStateException("Expected an instance of EnhancedMailSession: " + mailSession);
            }
            EnhancedMailSession enhancedMailSession = (EnhancedMailSession) mailSession;
            applyConsumer(enhancedMailSession, consumer, key);
            return this;
        }
    }

    @Configurable
    public MailFraction smtpServer(String key, EnhancedSMTPServerConsumer consumer) {
        return this.mailSession(key, (session) -> {
            session.smtpServer(consumer);
        });
    }

    private void applyConsumer(EnhancedMailSession mailSession, EnhancedMailSessionConsumer consumer, String key) {
        if (consumer != null) {
            consumer.accept(mailSession);
            if (mailSession.jndiName() == null) {
                mailSession.jndiName("java:jboss/mail/" + key);
            }
        }
    }

}