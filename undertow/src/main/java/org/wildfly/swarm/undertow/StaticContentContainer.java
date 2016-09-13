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
package org.wildfly.swarm.undertow;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.env.NativeDeploymentFactory;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.undertow.internal.UndertowExternalMountsAsset;

/**
 * @author Bob McWhirter
 */
public interface StaticContentContainer<T extends Archive<T>> extends Archive<T> {

    Logger log = Logger.getLogger(StaticContentContainer.class.getName());

    String EXTERNAL_MOUNT_PATH = "WEB-INF/undertow-external-mounts.conf";

    default T staticContent() {
        return staticContent("");
    }

    @SuppressWarnings("unchecked")
    default T staticContent(String base) {
        //as(WARArchive.class).addModule("org.wildfly.swarm.undertow", "runtime");

        try {
            // Add all the static content from the current app to the archive
            NativeDeploymentFactory nativeDeploymentFactory = ApplicationEnvironment.get().nativeDeploymentFactory();

            boolean accomplished = false;

            if ( nativeDeploymentFactory != null ) {
                Archive nativeDeployment = nativeDeploymentFactory.nativeDeployment();
                if (nativeDeployment != null) {
                    mergeIgnoringDuplicates(nativeDeployment, base, Filters.exclude(".*\\.class$"));
                    accomplished = true;
                }
            }
            if ( ! accomplished ) {
                FileSystemLayout fsLayout = FileSystemLayout.create();
                final Path webapp = fsLayout.resolveSrcWebAppDir();

                final Path root = webapp.resolve( base );

                if (Files.exists(root)) {
                    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Path simple = root.relativize(file);
                            add(new FileAsset(file.toFile()), convertSeparators( simple ) );
                            return super.visitFile(file, attrs);
                        }
                    });
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Error setting up static resources", ex);
        }

        Node node = get(EXTERNAL_MOUNT_PATH);
        UndertowExternalMountsAsset asset;
        if (node == null) {
            asset = new UndertowExternalMountsAsset();
            add(asset, EXTERNAL_MOUNT_PATH);
        } else {
            Asset tempAsset = node.getAsset();
            if (!(tempAsset instanceof UndertowExternalMountsAsset)) {
                asset = new UndertowExternalMountsAsset(tempAsset.openStream());
                add(asset, EXTERNAL_MOUNT_PATH);
            } else {
                asset = (UndertowExternalMountsAsset) node.getAsset();
            }
        }

        // Add external mounts for static content so changes are picked up
        // immediately during development
        Path webResources = Paths.get(System.getProperty("user.dir"), "src", "main", "webapp");
        if (base != null) {
            webResources = webResources.resolve(base);
        }
        if (Files.exists(webResources)) {
            asset.externalMount(webResources.toString());
        }
        webResources = Paths.get(System.getProperty("user.dir"), "src", "main", "resources");
        if (base != null) {
            webResources = webResources.resolve(base);
        }
        if (Files.exists(webResources)) {
            asset.externalMount(webResources.toString());
        }

        return (T) this;
    }

    static String convertSeparators(Path path) {
        String convertedPath = path.toString();

        if (convertedPath.contains(File.separator)) {
            convertedPath = convertedPath.replace(File.separator, "/");
        }

        return convertedPath;
    }


    @SuppressWarnings("unchecked")
    default T mergeIgnoringDuplicates(Archive<?> source, String base, Filter<ArchivePath> filter) {
        if (!base.startsWith("/")) {
            base = "/" + base;
        }
        // Get existing contents from source archive
        final Map<ArchivePath, Node> sourceContent = source.getContent();

        // Add each asset from the source archive
        for (final Map.Entry<ArchivePath, Node> contentEntry : sourceContent.entrySet()) {
            final Node node = contentEntry.getValue();
            ArchivePath nodePath = contentEntry.getKey();
            if (!nodePath.get().startsWith(base)) {
                continue;
            }
            if (!filter.include(nodePath)) {
                continue;
            }
            if (contains(nodePath)) {
                continue;
            }
            nodePath = new BasicPath(nodePath.get().replaceFirst(base, ""));
            // Delegate
            if (node.getAsset() == null) {
                addAsDirectory(nodePath);
            } else {
                add(node.getAsset(), nodePath);
            }
        }
        return (T) this;
    }
}
