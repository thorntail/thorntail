package org.jboss.unimbus.jca;

import java.io.File;

import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.spec.Connector;

/**
 * Created by bob on 2/8/18.
 */
public class ResourceAdapterDeployment {
    public ResourceAdapterDeployment(String uniqueId, File root, Connector connector) {
        this( uniqueId, root, connector, null);
    }

    public ResourceAdapterDeployment(String uniqueId, File root, Connector connector, Activation activation) {
        this.uniqueId = uniqueId;
        this.root = root;
        this.connector = connector;
        this.activation = activation;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public File getRoot() {
        return this.root;
    }

    public Connector getConnector() {
        return this.connector;
    }

    public Activation getActivation() {
        return this.activation;
    }

    private final String uniqueId;

    private final File root;

    private final Connector connector;

    private final Activation activation;
}
