/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.container.runtime;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
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

    private final List<ServerConfiguration> configurations;

    private final TempFileProvider tempFileProvider;

    private final List<Closeable> mountPoints = new ArrayList<>();

    public RuntimeDeployer(List<ServerConfiguration> configurations, ModelControllerClient client, SimpleContentProvider contentProvider, TempFileProvider tempFileProvider) throws IOException {
        this.configurations = configurations;
        this.client = client;
        this.contentProvider = contentProvider;
        this.tempFileProvider = tempFileProvider;
        //this.executor = Executors.newSingleThreadScheduledExecutor();
        //this.tempFileProvider = TempFileProvider.create("wildfly-swarm", this.executor);
    }

    @Override
    public void deploy(Archive deployment) throws IOException {

        for (ServerConfiguration each : this.configurations) {
            each.prepareArchive(deployment);
        }

        /*
        Map<ArchivePath, Node> c = deployment.getContent();
        for (Map.Entry<ArchivePath, Node> each : c.entrySet()) {
            System.err.println(each.getKey() + " // " + each.getValue());
        }
        */

        String dump = System.getProperty("swarm.export.deployment");
        if (dump != null &&
                !"false".equals(dump)) {
            File out = new File(deployment.getName());
            System.err.println("Dumping to " + out.getAbsolutePath());
            deployment.as(ZipExporter.class).exportTo(out, true);
        }

        VirtualFile mountPoint = VFS.getRootVirtualFile().getChild(deployment.getName());

        try (InputStream in = deployment.as(ZipExporter.class).exportAsInputStream()) {
            Closeable closeable = VFS.mountZipExpanded(in, deployment.getName(), mountPoint, tempFileProvider);
            this.mountPoints.add(closeable);
            //System.err.println( "mount: " + mountPoint + " // " + mountPoint.getPhysicalFile() );
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
        // TODO handle a non-success result
    }

    void stop() {
        for (Closeable each : this.mountPoints) {
            try {
                each.close();
            } catch (IOException e) {
            }
        }

    }

}
