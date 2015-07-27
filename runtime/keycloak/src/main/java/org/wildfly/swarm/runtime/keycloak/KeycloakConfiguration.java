package org.wildfly.swarm.runtime.keycloak;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.keycloak.KeycloakFraction;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Bob McWhirter
 */
public class KeycloakConfiguration extends AbstractServerConfiguration<KeycloakFraction> {

    public KeycloakConfiguration() {
        super(KeycloakFraction.class);
    }

    @Override
    public KeycloakFraction defaultFraction() {
        return new KeycloakFraction();
    }

    @Override
    public List<ModelNode> getList(KeycloakFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "keycloak"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.keycloak.keycloak-adapter-subsystem");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        return list;

    }
}
