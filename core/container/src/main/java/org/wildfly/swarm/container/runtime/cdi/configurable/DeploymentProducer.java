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
package org.wildfly.swarm.container.runtime.cdi.configurable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContext;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * Created by bob on 5/15/17.
 */
@ApplicationScoped
public class DeploymentProducer {

    private static final Logger LOGGER = Logger.getLogger("org.wildfly.swarm.deployment");

    private static final String CLASS_SUFFIX = ".class";

    private static final String JAR_SUFFIX = ".jar";

    static final String INDEX_LOCATION = "META-INF/jandex.idx";

    @Inject
    DeploymentContext context;

    @Produces
    @DeploymentScoped
    Archive archive() {
        return context.getCurrentArchive();
    }

    @Produces
    @DeploymentScoped
    IndexView index() {
        return createDeploymentIndex(context.getCurrentArchive());
    }

    IndexView createDeploymentIndex(Archive<?> deployment) {
        List<IndexView> indexes = new ArrayList<IndexView>();
        try {
            index(deployment, indexes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return CompositeIndex.create(indexes);
    }

    private void index(Archive<?> archive, List<IndexView> indexes) throws IOException {
        LOGGER.debugv("Indexing archive: {0}", archive.getName());

        // First try to load attached index
        Node indexNode = archive.get(ArchivePaths.create(INDEX_LOCATION));
        if (indexNode != null) {
            try (InputStream indexStream = indexNode.getAsset().openStream()) {
                LOGGER.debugv("Loading attached index from archive: {0}", archive.getName());
                indexes.add(new IndexReader(indexStream).read());
            }
        } else {
            // No index found - index all classes found
            Indexer indexer = new Indexer();
            for (Map.Entry<ArchivePath, Node> entry : archive.getContent(this::isClass).entrySet()) {
                try (InputStream contentStream = entry.getValue().getAsset().openStream()) {
                    LOGGER.debugv("Indexing asset: {0} from archive: {1}", entry.getKey().get(), archive.getName());
                    indexer.index(contentStream);
                } catch (Exception indexerException) {
                    LOGGER.warnv(indexerException,
                            "Failed indexing {0} from archive {1}",
                            entry.getKey().get(),
                            archive.getName());
                }
            }
            Index index = indexer.complete();
            indexes.add(index);
        }

        if (archive instanceof LibraryContainer) {
            for (Map.Entry<ArchivePath, Node> entry : archive.getContent(a -> a.get().endsWith(JAR_SUFFIX)).entrySet()) {
                Asset asset = entry.getValue().getAsset();
                if (asset instanceof ArchiveAsset) {
                    ArchiveAsset archiveAsset = (ArchiveAsset) asset;
                    index(archiveAsset.getArchive(), indexes);
                } else if (asset != null) {
                    // `asset` can be `null` in case the `archive` contains a _directory_ named *.jar
                    try (InputStream contentStream = asset.openStream()) {
                        JARArchive jarArchive = ShrinkWrap.create(JARArchive.class, entry.getKey().get())
                                .as(ZipImporter.class)
                                .importFrom(contentStream)
                                .as(JARArchive.class);
                        index(jarArchive, indexes);
                    }
                }
            }
        }
    }

    private boolean isClass(ArchivePath a) {
        String path = a.get();
        return path.endsWith(CLASS_SUFFIX) && !path.endsWith("module-info.class");
    }

}
