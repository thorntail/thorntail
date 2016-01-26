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
package org.wildfly.swarm.arquillian.daemon.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.ServiceProviderAsset;

/**
 * {@link DeploymentPackager} to merge auxiliar archive contents with the archive provided by the user
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 * @author Toby Crawley
 */
public enum DaemonDeploymentPackager implements DeploymentPackager {

    INSTANCE;

    private static final Logger log = Logger.getLogger(DaemonDeploymentPackager.class.getName());

    @Override
    public Archive<?> generateDeployment(final TestDeployment testDeployment,
                                         final Collection<ProtocolArchiveProcessor> processors) {
        // Merge auxiliary archives with the declared for ARQ and testrunner support
        final Archive archive = testDeployment.getApplicationArchive();
        if (log.isLoggable(Level.FINEST)) {
            log.finest("Archive before additional packaging: " + archive.toString(true));
        }
        if (Validate.isArchiveOfType(WebArchive.class, archive)) {
            ((LibraryContainer<?>) archive).addAsLibraries(testDeployment.getAuxiliaryArchives());
        } else {
            mergeAuxAsClasses(archive, testDeployment);
        }

        if (log.isLoggable(Level.FINEST)) {
            log.finest("Archive after additional packaging: " + archive.toString(true));
        }

        return archive;
    }

    private void mergeAuxAsClasses(final Archive<?> archive, final TestDeployment testDeployment) {
        final Map<ArchivePath, List<Node>> serviceDescriptors = new HashMap<>();
        findServiceDescriptors(archive, serviceDescriptors);
        testDeployment.getAuxiliaryArchives().forEach(aux -> {
            findServiceDescriptors(aux, serviceDescriptors);
            archive.merge(aux);
        });

        mergeServiceDescriptors(serviceDescriptors)
                .forEach((path, asset) -> archive.add(asset, path));

    }

    private Map<ArchivePath, Asset> mergeServiceDescriptors(final Map<ArchivePath, List<Node>> descs) {
        return descs.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> mergeServiceDescriptors(entry.getValue())));
    }

    private Asset mergeServiceDescriptors(final List<Node> descs) {
        final List<String> lines = new ArrayList<>();
        descs.forEach(n -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(n.getAsset().openStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return new ServiceProviderAsset(lines.toArray(new String[lines.size()]));
    }

    private void findServiceDescriptors(final Archive<?> archive, final Map<ArchivePath, List<Node>> descs) {
        archive.getContent(path -> path.get().startsWith("/META-INF/services/"))
                .forEach((path, node) -> {
                    List<Node> nodes = descs.get(path);
                    if (nodes == null) {
                        nodes = new ArrayList<>();
                        descs.put(path, nodes);
                    }
                    nodes.add(node);
                });
    }
}
