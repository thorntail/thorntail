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
package org.wildfly.swarm.resource.adapters.runtime;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.env.NativeDeploymentFactory;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.resource.adapters.RARArchive;
import org.wildfly.swarm.spi.api.DependenciesContainer;
import org.wildfly.swarm.spi.runtime.DefaultDeploymentFactory;

/**
 * @author Ralf Battenfeld
 */
@ApplicationScoped
public class DefaultRarDeploymentFactory extends DefaultDeploymentFactory {


    @Inject
    NativeDeploymentFactory nativeDeploymentFactory;

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getType() {
        return "rar";
    }

    @Override
    public Archive<?> create() throws Exception {
        Archive archive = this.nativeDeploymentFactory.nativeDeployment();
        if ( archive != null ) {
            return archive;
        }

        archive = this.nativeDeploymentFactory.createEmptyArchive(RARArchive.class, ".rar" );
        setupUsingMaven( archive );
        return archive;
    }

    @Override
    public Archive createFromJar() throws Exception {
        return create();
    }

    public boolean setupUsingMaven(final Archive<?> givenArchive) throws Exception {
        final DependenciesContainer<?> archive = (DependenciesContainer<?>) givenArchive;

        FileSystemLayout fsLayout = FileSystemLayout.create();
        final Path classes = fsLayout.resolveBuildClassesDir();

        boolean success = false;

        if (Files.exists(classes)) {
            success = true;
            Files.walkFileTree(classes, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path simple = classes.relativize(file);
                    archive.add(new FileAsset(file.toFile()), "WEB-INF/classes/" + convertSeparators(simple));
                    if (simple.toString().contains("WEB-INF")) {
                        archive.add(new FileAsset(file.toFile()), convertSeparators(simple));
                    }
                    return super.visitFile(file, attrs);
                }
            });
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

    protected static String determineName() {
        return DefaultDeploymentFactory.determineName(".rar");
    }

}
