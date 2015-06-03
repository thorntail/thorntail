package org.wildfly.swarm.runtime.security;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;
import org.wildfly.swarm.security.SecurityFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class SecurityConfiguration extends AbstractServerConfiguration<SecurityFraction> {

    public SecurityConfiguration() {
        super(SecurityFraction.class);
    }

    @Override
    public SecurityFraction defaultFraction() {
        return new SecurityFraction();
    }

    @Override
    public List<ModelNode> getList(SecurityFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "security"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.security");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("security-domain", "other").toModelNode());
        node.get(OP).set(ADD);
        node.get("cache-type").set("default");
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("security-domain", "other").append("authentication", "classic").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("security-domain", "other").append("authentication", "classic").append("login-module", "RealmDirect").toModelNode());
        node.get(OP).set(ADD);
        node.get("code").set("RealmDirect");
        node.get("flag").set("required");
        node.get("module-options").set("password-stacking", "useFirstPass");
        list.add(node);

        return list;
    }
}
