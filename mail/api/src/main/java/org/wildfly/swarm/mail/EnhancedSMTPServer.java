package org.wildfly.swarm.mail;

import org.wildfly.swarm.config.mail.mail_session.SMTPServer;
import org.wildfly.swarm.container.OutboundSocketBinding;

/**
 * @author Bob McWhirter
 */
public class EnhancedSMTPServer extends SMTPServer<EnhancedSMTPServer> {

    private final String sessionKey;

    private String host;

    private String port;

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
        if ( this.host == null && this.port == null ) {
            return null;
        }

        this.outboundSocketBindingRef( "mail-smtp-" + this.sessionKey );

        return new OutboundSocketBinding( "mail-smtp-" + this.sessionKey )
                .remoteHost( this.host )
                .remotePort( this.port );
    }


}
