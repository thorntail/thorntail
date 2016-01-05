package org.wildfly.swarm.mail;

import org.wildfly.swarm.config.mail.MailSession;
import org.wildfly.swarm.config.mail.mail_session.SMTPServerConsumer;

/**
 * @author Bob McWhirter
 */
public class EnhancedMailSession extends MailSession<EnhancedMailSession> {

    public EnhancedMailSession(String key) {
        super(key);
    }

    public EnhancedMailSession smtpServer(EnhancedSMTPServerConsumer consumer) {
        EnhancedSMTPServer server = new EnhancedSMTPServer( getKey() );
        return super.smtpServer( ()->{
            if ( consumer != null ) {
                consumer.accept( server );
            }
            return server;
        });
    }
}
