/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.wildfly.swarm.undertow.internal.DefaultWarDeploymentFactory;
import org.wildfly.swarm.undertow.internal.UndertowExternalMountsAsset;

/** Archive mix-in supporting static content serving for .war files.
 *
 * @author Bob McWhirter
 */
public interface StaticContentContainer<T extends Archive<T>> extends Archive<T> {

    Logger log = Logger.getLogger(StaticContentContainer.class.getName());

    String EXTERNAL_MOUNT_PATH = "WEB-INF/undertow-external-mounts.conf";

    /** Enable static content to be served from the root of the classpath.
     *
     * @return This archive.
     */
    default T staticContent() {
        return staticContent("");
    }

    /** Enable static content to be served from a given base in the classpath.
     *
     * @param base The path prefix to use for static content.
     * @return
     */
    @SuppressWarnings("unchecked")
    default T staticContent(String base) {
        //as(WARArchive.class).addModule("org.wildfly.swarm.undertow", "runtime");

        try {
            // Add all the static content from the current app to the archive
            // I believe it does not make sense to call archiveFromCurrentApp() again if this archive was created by DefaultWarDeploymentFactory
            Archive allResources = contains(DefaultWarDeploymentFactory.MARKER_PATH) ? this : DefaultWarDeploymentFactory.archiveFromCurrentApp();

            // Here we define static as basically anything that's not a
            // Java class file or under WEB-INF or META-INF
            mergeIgnoringDuplicates(allResources, base, Filters.exclude(".*\\.class$"));
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
