package org.wildfly.swarm.runtime.mail;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.mail.MailFraction;
import org.wildfly.swarm.mail.SmtpServer;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

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

        node = new ModelNode();
        node.get(OP_ADDR).set(smtpServerAddress.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        addSmtpServers(fraction, list);

        return list;
    }

    protected void addSmtpServers(MailFraction fraction, List<ModelNode> list) {
        for (SmtpServer each : fraction.smtpServers()) {
            addSmtpServer(each, list);
        }
    }

    protected void addSmtpServer(SmtpServer smtpServer, List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(smtpServerAddress.append("mail-session", smtpServer.name().toLowerCase()).toModelNode());
        node.get(OP).set(ADD);
        node.get("jndi-name").set(smtpServer.jndiName());
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(smtpServerAddress.append("mail-session", smtpServer.name().toLowerCase()).append("server", "smtp").toModelNode());
        node.get(OP).set(ADD);
        node.get("outbound-socket-binding-ref").set(smtpServer.outboundSocketBindingRef());
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(PathAddress.pathAddress("socket-binding-group", "default-sockets").append("remote-destination-outbound-socket-binding", smtpServer.outboundSocketBindingRef()).toModelNode());
        node.get(OP).set(ADD);
        node.get("host").set(smtpServer.host());
        node.get("port").set(smtpServer.port());
        list.add(node);
    }
}
