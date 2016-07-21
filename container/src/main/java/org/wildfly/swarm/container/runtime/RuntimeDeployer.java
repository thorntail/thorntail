/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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

import javax.enterprise.inject.Vetoed;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.internal.Deployer;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.internal.SwarmInternalProperties;
import org.wildfly.swarm.spi.runtime.ServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.BLOCKING_TIMEOUT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CONTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HASH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RUNTIME_NAME;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class RuntimeDeployer implements Deployer {

    public RuntimeDeployer(RuntimeServer.Opener opener, ServiceContainer serviceContainer, List<ServerConfiguration<Fraction>> configurations, ModelControllerClient client, SimpleContentProvider contentProvider, TempFileProvider tempFileProvider) throws IOException {
        this.opener = opener;
        this.serviceContainer = serviceContainer;
        this.configurations = configurations;
        this.client = client;
        this.contentProvider = contentProvider;
        this.tempFileProvider = tempFileProvider;
    }

    public void debug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void deploy(Archive<?> deployment) throws DeploymentException {

        // 1. give fractions a chance to handle the deployment
        for (ServerConfiguration each : this.configurations) {
            each.prepareArchive(deployment);
        }

        // 2. create a meta data index
        Indexer indexer = new Indexer();
        Map<ArchivePath, Node> c = deployment.getContent();
        try {
            for (Map.Entry<ArchivePath, Node> each : c.entrySet()) {
                if (each.getKey().get().endsWith(CLASS_SUFFIX)) {
                    indexer.index(each.getValue().getAsset().openStream());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Index index = indexer.complete();

        // 2.1 let fractions process the meta data
        for (ServerConfiguration each : this.configurations) {
            each.processArchiveMetaData(deployment, index);
        }

        if (this.debug) {
            Map<ArchivePath, Node> ctx = deployment.getContent();
            for (Map.Entry<ArchivePath, Node> each : ctx.entrySet()) {
                System.err.println(each.getKey() + " // " + each.getValue());
            }
        }

        if (BootstrapProperties.flagIsSet(SwarmProperties.EXPORT_DEPLOYMENT)) {
            final File out = new File(deployment.getName());
            System.err.println("Exporting deployment to " + out.getAbsolutePath());
            deployment.as(ZipExporter.class).exportTo(out, true);
        }

        VirtualFile mountPoint = VFS.getRootVirtualFile().getChild(deployment.getName());

        try (InputStream in = deployment.as(ZipExporter.class).exportAsInputStream()) {
            Closeable closeable = VFS.mountZipExpanded(in, deployment.getName(), mountPoint, tempFileProvider);
            this.mountPoints.add(closeable);
        } catch (IOException e) {
            throw new DeploymentException(deployment, e);
        }

        byte[] hash = this.contentProvider.addContent(mountPoint);

        final ModelNode deploymentAdd = new ModelNode();

        deploymentAdd.get(OP).set(ADD);
        deploymentAdd.get(OP_ADDR).set("deployment", deployment.getName());
        deploymentAdd.get(RUNTIME_NAME).set(deployment.getName());
        deploymentAdd.get(ENABLED).set(true);

        int deploymentTimeout = Integer.getInteger(SwarmProperties.DEPLOYMENT_TIMEOUT, 300);

        final ModelNode opHeaders = new ModelNode();
        opHeaders.get(BLOCKING_TIMEOUT).set(deploymentTimeout);
        deploymentAdd.get(OPERATION_HEADERS).set(opHeaders);

        ModelNode content = deploymentAdd.get(CONTENT).add();
        content.get(HASH).set(hash);

        BootstrapLogger.logger("org.wildfly.swarm.runtime.deployer")
                .info("deploying " + deployment.getName());
        System.setProperty(SwarmInternalProperties.CURRENT_DEPLOYMENT, deployment.getName());
        try {
            ModelNode result = client.execute(deploymentAdd);

            ModelNode outcome = result.get("outcome");

            if (outcome.asString().equals("success")) {
                // When there's a successful deployment, enable every undertow http listener
                // if it's not already enabled, regardless of name.
                openConnections(deployment);
                return;
            }

            ModelNode description = result.get("failure-description");
            throw new DeploymentException(deployment, description.asString());
        } catch (IOException e) {
            throw new DeploymentException(deployment, e);
        }
    }

    public void openConnections(Archive<?> archive) {
        if (archive.getName().endsWith(".war") || archive.getName().endsWith(".ear")) {
            openConnections();
        }
    }

    public void openConnections() {
        if (this.opener != null) {
            this.opener.open();
        }
    }

    void stop() {
        for (Closeable each : this.mountPoints) {
            try {
                each.close();
            } catch (IOException e) {
            }
        }

    }

    private static final String CLASS_SUFFIX = ".class";

    private final ServiceContainer serviceContainer;

    private final ModelControllerClient client;

    private final SimpleContentProvider contentProvider;

    private final List<ServerConfiguration<Fraction>> configurations;

    private final TempFileProvider tempFileProvider;

    private final List<Closeable> mountPoints = new ArrayList<>();

    private boolean debug = false;

    private final RuntimeServer.Opener opener;

}
