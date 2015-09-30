package org.wildfly.swarm.transactions.runtime;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.transactions.TransactionsFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class TransactionsConfiguration extends AbstractServerConfiguration<TransactionsFraction> {

    public TransactionsConfiguration() {
        super(TransactionsFraction.class);
    }

    @Override
    public TransactionsFraction defaultFraction() {

        return TransactionsFraction.createDefaultFraction();
    }

    @Override
    public List<ModelNode> getList(TransactionsFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.transactions");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Unable to configure Transactions subsystem. " + e);
            e.printStackTrace();
        }

        node = new ModelNode();
        node.get(OP_ADDR).set(PathAddress.pathAddress(PathElement.pathElement("socket-binding-group", "default-sockets")).append("socket-binding", "txn-recovery-environment").toModelNode());
        node.get(OP).set(ADD);
        node.get(PORT).set(fraction.getPort());
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(PathAddress.pathAddress(PathElement.pathElement("socket-binding-group", "default-sockets")).append("socket-binding", "txn-status-manager").toModelNode());
        node.get(OP).set(ADD);
        node.get(PORT).set(fraction.getStatusPort());
        list.add(node);


        return list;

    }
}
