package org.wildfly.swarm.mail;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.config.mail.MailSession;
import org.wildfly.swarm.config.mail.mail_session.SMTPServer;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.Post;
import org.wildfly.swarm.spi.api.SocketBindingGroup;

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
                    OutboundSocketBinding socketBinding = ((EnhancedSMTPServer) server).outboundSocketBinding();
                    if (socketBinding != null) {
                        this.group.outboundSocketBinding(socketBinding);
                    }
                }
            }
        }
    }
}
