package org.jboss.unimbus.jca.ironjacamar;

import java.io.File;

import org.jboss.jca.common.api.metadata.spec.Connector;

/**
 * Created by bob on 2/8/18.
 */
public class ResourceAdapterDeployment {

    public ResourceAdapterDeployment(String uniqueId, File root, Connector connector) {
        this.uniqueId = uniqueId;
        this.root = root;
        this.connector = connector;
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

    private final String uniqueId;

    private final File root;

    private final Connector connector;
}
