package org.wildfly.swarm.runtime.container;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.wildfly.swarm.container.Deployer;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CONTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HASH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RUNTIME_NAME;

/**
 * @author Bob McWhirter
 */
public class RuntimeDeployer implements Deployer {

    private final ModelControllerClient client;

    private final SimpleContentProvider contentProvider;

    private final ScheduledExecutorService executor;

    private final TempFileProvider tempFileProvider;
    private final List<Closeable> mountPoints = new ArrayList<>();

    public RuntimeDeployer(ModelControllerClient client, SimpleContentProvider contentProvider) throws IOException {
        this.client = client;
        this.contentProvider = contentProvider;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.tempFileProvider = TempFileProvider.create("wildfly-swarm", this.executor);
    }

    @Override
    public void deploy(Archive deployment) throws IOException {

        VirtualFile mountPoint = VFS.getRootVirtualFile().getChild(deployment.getName());
        try (InputStream in = new ZipExporterImpl(deployment).exportAsInputStream()) {
            Closeable closeable = VFS.mountZipExpanded(in, deployment.getName(), mountPoint, tempFileProvider);
            this.mountPoints.add( closeable );
        }

        byte[] hash = this.contentProvider.addContent(mountPoint);

        final ModelNode deploymentAdd = new ModelNode();

        deploymentAdd.get(OP).set(ADD);
        deploymentAdd.get(OP_ADDR).set("deployment", deployment.getName());
        deploymentAdd.get(RUNTIME_NAME).set(deployment.getName());
        deploymentAdd.get(ENABLED).set(true);

        ModelNode content = deploymentAdd.get(CONTENT).add();
        content.get(HASH).set(hash);

        System.setProperty("wildfly.swarm.current.deployment", deployment.getName());
        ModelNode result = client.execute(deploymentAdd);
    }

    void stop() {
        for ( Closeable each : this.mountPoints ) {
            try {
                each.close();
            } catch (IOException e) {
            }
        }

    }

}
