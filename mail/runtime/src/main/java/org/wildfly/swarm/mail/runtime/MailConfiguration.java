package org.wildfly.swarm.mail.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.config.invocation.Marshaller;
import org.wildfly.swarm.config.Mail;
import org.wildfly.swarm.config.mail.MailSession;
import org.wildfly.swarm.config.mail.mail_session.SmtpServer;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.mail.MailFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Ken Finnigan
 */
public class MailConfiguration extends AbstractServerConfiguration<MailFraction> {

    private PathAddress smtpServerAddress = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "mail"));

    public MailConfiguration() {
        super(MailFraction.class);
    }

    @Override
    public MailFraction defaultFraction() {
        return new MailFraction();
    }

    @Override
    public List<ModelNode> getList(MailFraction fraction) {

        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.mail");
        node.get(OP).set(ADD);
        list.add(node);

        Mail mail = new Mail();
        List<ModelNode> socketBindings = addSmtpServers(fraction, mail);

        try {
            list.addAll(Marshaller.marshal(mail));
            list.addAll(socketBindings);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    protected List<ModelNode> addSmtpServers(MailFraction fraction, Mail mail) {
        List<ModelNode> list = new ArrayList<>();
        for (org.wildfly.swarm.mail.SmtpServer each : fraction.smtpServers()) {
            list.add(addSmtpServer(each, mail));
        }

        return list;
    }

    protected ModelNode addSmtpServer(org.wildfly.swarm.mail.SmtpServer smtpServer, Mail mail) {

        SmtpServer smtp = new SmtpServer().outboundSocketBindingRef(smtpServer.outboundSocketBindingRef());

        MailSession mailSession = new MailSession(smtpServer.name().toLowerCase())
                .smtpServer(smtp)
                .jndiName(smtpServer.jndiName());

        mail.mailSession(mailSession);


        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(PathAddress.pathAddress("socket-binding-group", "default-sockets").append("remote-destination-outbound-socket-binding", smtpServer.outboundSocketBindingRef()).toModelNode());
        node.get(OP).set(ADD);
        node.get("host").set(smtpServer.host());
        node.get("port").set(smtpServer.port());
        return node;
    }
}
