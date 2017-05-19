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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.vfs.TempFileProvider;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.internal.Deployer;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContext;
import org.wildfly.swarm.container.runtime.deployments.DefaultDeploymentCreator;
import org.wildfly.swarm.container.runtime.wildfly.SwarmContentRepository;
import org.wildfly.swarm.internal.DeployerMessages;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.DependenciesContainer;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.internal.SwarmInternalProperties;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.BLOCKING_TIMEOUT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CONTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HASH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PERSISTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RUNTIME_NAME;

/**
 * @author Bob McWhirter
 * @author Heiko Braun
 * @author Ken Finnigan
 */
@ApplicationScoped
public class RuntimeDeployer implements Deployer {

    //private static Logger LOG = Logger.getLogger("org.wildfly.swarm.deployer");

    private static final String ALL_DEPENDENCIES_ADDED_MARKER = DependenciesContainer.ALL_DEPENDENCIES_MARKER + ".added";

    @Override
    public void deploy() throws DeploymentException {
        Archive<?> deployment = createDefaultDeployment();
        if (deployment == null) {
            throw DeployerMessages.MESSAGES.unableToCreateDefaultDeployment();
        } else {
            deploy(deployment);
        }
    }

    @Override
    public void deploy(Collection<Path> pathsToDeploy) throws DeploymentException {
        if (pathsToDeploy.isEmpty()) {
            DeployerMessages.MESSAGES.noDeploymentsSpecified();
            return;
        }
        archives(pathsToDeploy)
                .forEach(e -> {
                    deploy(e);
                });
    }

    protected static Stream<Archive> archives(Collection<Path> paths) {
        return paths.stream()
                .map(path -> {
                    String simpleName = path.getFileName().toString();
                    Archive archive = ShrinkWrap.create(JavaArchive.class, simpleName);
                    archive.as(ZipImporter.class).importFrom(path.toFile());
                    return archive;
                });
    }

    public Archive<?> createDefaultDeployment() {
        return this.defaultDeploymentCreator.createDefaultDeployment(determineDeploymentType());
    }

    private String determineDeploymentType() {
        if (this.defaultDeploymentType == null) {
            this.defaultDeploymentType = determineDeploymentTypeInternal();
            System.setProperty(BootstrapProperties.DEFAULT_DEPLOYMENT_TYPE, this.defaultDeploymentType);
        }
        return this.defaultDeploymentType;
    }

    private String determineDeploymentTypeInternal() {
        String artifact = System.getProperty(BootstrapProperties.APP_PATH);
        if (artifact != null) {
            int dotLoc = artifact.lastIndexOf('.');
            if (dotLoc >= 0) {
                return artifact.substring(dotLoc + 1);
            }
        }

        artifact = System.getProperty(BootstrapProperties.APP_ARTIFACT);
        if (artifact != null) {
            int dotLoc = artifact.lastIndexOf('.');
            if (dotLoc >= 0) {
                return artifact.substring(dotLoc + 1);
            }
        }

        // fallback to file system
        FileSystemLayout fsLayout = FileSystemLayout.create();

        return fsLayout.determinePackagingType();
    }

    public void debug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void deploy(Archive<?> deployment) throws DeploymentException {

        try (AutoCloseable deploymentTimer = Performance.time("deployment: " + deployment.getName())) {

            // check for "org.wildfly.swarm.allDependencies" flag
            // see DependenciesContainer#addAllDependencies()
            if (deployment instanceof DependenciesContainer) {
                DependenciesContainer<?> depContainer = (DependenciesContainer) deployment;
                if (depContainer.hasMarker(DependenciesContainer.ALL_DEPENDENCIES_MARKER)) {
                    if (!depContainer.hasMarker(ALL_DEPENDENCIES_ADDED_MARKER)) {
                        ApplicationEnvironment appEnv = ApplicationEnvironment.get();

                        if (ApplicationEnvironment.Mode.UBERJAR == appEnv.getMode()) {
                            ArtifactLookup artifactLookup = ArtifactLookup.get();
                            for (String gav : appEnv.getDependencies()) {
                                depContainer.addAsLibrary(artifactLookup.artifact(gav));
                            }
                        } else {
                            Set<String> paths = appEnv.resolveDependencies(Collections.emptyList());
                            for (String path : paths) {
                                final File pathFile = new File(path);
                                if (path.endsWith(".jar")) {
                                    depContainer.addAsLibrary(pathFile);
                                } else if (pathFile.isDirectory()) {
                                    depContainer
                                            .merge(ShrinkWrap.create(GenericArchive.class)
                                                           .as(ExplodedImporter.class)
                                                           .importDirectory(pathFile)
                                                           .as(GenericArchive.class),
                                                   "/WEB-INF/classes",
                                                   Filters.includeAll());
                                }
                            }
                        }

                        depContainer.addMarker(ALL_DEPENDENCIES_ADDED_MARKER);
                    }
                }
            }

            this.deploymentContext.activate(deployment);

            // 2. give fractions a chance to handle the deployment
            for (DeploymentProcessor processor : this.deploymentProcessors) {
                processor.process();
            }

            this.deploymentContext.deactivate();

            if (DeployerMessages.MESSAGES.isDebugEnabled()) {
                DeployerMessages.MESSAGES.deploying(deployment.getName());
                Map<ArchivePath, Node> ctx = deployment.getContent();
                for (Map.Entry<ArchivePath, Node> each : ctx.entrySet()) {
                    DeployerMessages.MESSAGES.deploymentContent(each.getKey().toString());
                }
            }

            if (BootstrapProperties.flagIsSet(SwarmProperties.EXPORT_DEPLOYMENT)) {
                final File out = new File(deployment.getName());
                DeployerMessages.MESSAGES.exportingDeployment(out.getAbsolutePath());
                deployment.as(ZipExporter.class).exportTo(out, true);
            }

            byte[] hash = this.contentRepository.addContent(deployment.as(ZipExporter.class).exportAsInputStream());

            final ModelNode deploymentAdd = new ModelNode();

            deploymentAdd.get(OP).set(ADD);
            deploymentAdd.get(OP_ADDR).set("deployment", deployment.getName());
            deploymentAdd.get(RUNTIME_NAME).set(deployment.getName());
            deploymentAdd.get(ENABLED).set(true);
            deploymentAdd.get(PERSISTENT).set(true);
            ModelNode content = deploymentAdd.get(CONTENT).add();
            content.get(HASH).set(hash);

            int deploymentTimeout = Integer.getInteger(SwarmProperties.DEPLOYMENT_TIMEOUT, 300);

            final ModelNode opHeaders = new ModelNode();
            opHeaders.get(BLOCKING_TIMEOUT).set(deploymentTimeout);
            deploymentAdd.get(OPERATION_HEADERS).set(opHeaders);


            BootstrapLogger.logger("org.wildfly.swarm.runtime.deployer")
                    .info("deploying " + deployment.getName());
            System.setProperty(SwarmInternalProperties.CURRENT_DEPLOYMENT, deployment.getName());
            try {
                ModelNode result = client.execute(deploymentAdd);

                ModelNode outcome = result.get("outcome");

                if (outcome.asString().equals("success")) {
                    return;
                }

                ModelNode description = result.get("failure-description");
                throw new DeploymentException(deployment, SwarmMessages.MESSAGES.deploymentFailed(description.asString()));
            } catch (IOException e) {
                throw SwarmMessages.MESSAGES.deploymentFailed(e, deployment);
            }
        } catch (Exception e) {
            throw new DeploymentException(deployment, e);
        }
    }

    @SuppressWarnings("unused")
    @PreDestroy
    void stop() {
    }

    @Inject
    DeploymentContext deploymentContext;

    private String defaultDeploymentType;

    @SuppressWarnings("unused")
    @Inject
    private ModelControllerClient client;

    @SuppressWarnings("unused")
    @Inject
    private SwarmContentRepository contentRepository;

    @SuppressWarnings("unused")
    @Inject
    private TempFileProvider tempFileProvider;

    @SuppressWarnings("unused")
    @Inject
    private DefaultDeploymentCreator defaultDeploymentCreator;

    @SuppressWarnings("unused")
    private boolean debug = false;

    @SuppressWarnings("unused")
    @Inject
    @Any
    private Instance<DeploymentProcessor> deploymentProcessors;
}
