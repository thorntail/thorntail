package org.wildfly.swarm.container.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.Fraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public abstract class ExtensionServerConfiguration<T extends Fraction> extends AbstractServerConfiguration<T> {

    private final String extensionModuleName;

    public ExtensionServerConfiguration(Class<T> type, String extensionModuleName) {
        super(type);
        this.extensionModuleName = extensionModuleName;
    }

    public String getExtensionModuleName() {
        return this.extensionModuleName;
    }

    protected ModelNode getExtensionNode() {
        if ( this.extensionModuleName != null ) {
            ModelNode node = new ModelNode();
            node.get(OP_ADDR).set(EXTENSION, this.extensionModuleName);
            node.get(OP).set(ADD);
            return node;
        }

        return null;
    }

    protected void addExtensionNode(List<ModelNode> list)  {
        ModelNode node = getExtensionNode();
        if ( node != null ) {
            list.add( node );
        }
    }

    @Override
    public List<ModelNode> getList(T fraction) throws Exception {
        List<ModelNode> list = new ArrayList<>();
        addExtensionNode( list );
        return list;
    }
}
