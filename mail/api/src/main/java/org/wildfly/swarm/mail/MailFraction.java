package org.wildfly.swarm.mail;

import java.util.ArrayList;
import java.util.List;

import org.wildfly.swarm.container.Fraction;

/**
 * @author Ken Finnigan
 */
public class MailFraction implements Fraction {

    private List<SmtpServer> smtpServers = new ArrayList<>();

    public MailFraction() {
    }

    public MailFraction smtpServer(SmtpServer mailServer) {
        this.smtpServers.add(mailServer);
        return this;
    }

    public List<SmtpServer> smtpServers() {
        return this.smtpServers;
    }

}
