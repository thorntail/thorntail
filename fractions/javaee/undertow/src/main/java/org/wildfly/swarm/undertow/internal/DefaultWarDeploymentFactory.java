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
package org.wildfly.swarm.undertow.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.wildfly.swarm.internal.ExplodedApplicationArtifactLocator;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.spi.api.DefaultDeploymentFactory;
import org.wildfly.swarm.spi.api.DependenciesContainer;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 * @author Heiko Braun
 */
@ApplicationScoped
public class DefaultWarDeploymentFactory extends DefaultDeploymentFactory {

    public static final ArchivePath MARKER_PATH = ArchivePaths.create("META-INF/" + DefaultWarDeploymentFactory.class.getName());

    public static WARArchive archiveFromCurrentApp() throws Exception {
        final WARArchive archive = ShrinkWrap.create(WARArchive.class, determineName());
        final DefaultDeploymentFactory factory = new DefaultWarDeploymentFactory();
        factory.setup(archive);
        //archive.addModule("org.wildfly.swarm.undertow", "runtime");
        archive.add(EmptyAsset.INSTANCE, MARKER_PATH);
        return archive;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getType() {
        return "war";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Archive create() throws Exception {
        return archiveFromCurrentApp().staticContent();
    }

    public boolean setupUsingMaven(final Archive<?> givenArchive) throws Exception {
        final DependenciesContainer<?> archive = (DependenciesContainer<?>) givenArchive;

        FileSystemLayout fsLayout = FileSystemLayout.create();
        final Path classes = fsLayout.resolveBuildClassesDir();
        boolean success = false;

        if (Files.exists(classes)) {
            success = true;
            addFilesToArchive(classes, archive);
        }

        // If it a gradle project, the reources are seperated from the class files.
        final Path resources = fsLayout.resolveBuildResourcesDir();
        if (!Files.isSameFile(resources, classes) && Files.exists(resources)) {
            success = true;
            addFilesToArchive(resources, archive);
        }


        final Path webapp = fsLayout.resolveSrcWebAppDir();

        if (Files.exists(webapp)) {
            success = true;
            Files.walkFileTree(webapp, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path simple = webapp.relativize(file);
                    archive.add(new FileAsset(file.toFile()), convertSeparators(simple));
                    return super.visitFile(file, attrs);
                }
            });
        }

        archive.addAllDependencies();

        return success;
    }

    @Override
    protected boolean setupUsingAppArtifact(Archive<?> archive) throws IOException {
        File exploded = ExplodedApplicationArtifactLocator.get();
        if (exploded != null && exploded.canRead()) {
            // Use exploded deployment from tmp dir
            archive.as(ExplodedImporter.class).importDirectory(exploded);
            return true;
        }
        return super.setupUsingAppArtifact(archive);
    }

    private void addFilesToArchive(final Path files, final DependenciesContainer<?> archive) throws Exception {
        Files.walkFileTree(files, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path simple = files.relativize(file);
                archive.add(new FileAsset(file.toFile()), "WEB-INF/classes/" + convertSeparators(simple));
                // If the user's maven output is a jar then they may place
                // static content under src/main/resources, in which case
                // we need to hoist anything under WEB-INF out of there
                // and put it into the root of this archive instead of
                // under WEB-INF/classes/WEB-INF/foo
                if (simple.toString().contains("WEB-INF")) {
                    archive.add(new FileAsset(file.toFile()), convertSeparators(simple));
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    protected static String determineName() {
        return DefaultDeploymentFactory.determineName(".war");
    }

}
