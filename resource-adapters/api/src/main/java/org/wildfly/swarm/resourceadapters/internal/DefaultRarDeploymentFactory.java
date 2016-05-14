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
package org.wildfly.swarm.resourceadapters.internal;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.wildfly.swarm.resourceadapters.RARArchive;
import org.wildfly.swarm.spi.api.DefaultDeploymentFactory;
import org.wildfly.swarm.spi.api.DependenciesContainer;

/**
 * @author Ralf Battenfeld
 */
public class DefaultRarDeploymentFactory extends DefaultDeploymentFactory {

    public static RARArchive archiveFromCurrentApp() throws Exception {
        final RARArchive archive = ShrinkWrap.create(RARArchive.class, determineName());
        final DefaultDeploymentFactory factory = new DefaultRarDeploymentFactory();
        factory.setup(archive);
        return archive;
    }

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
        return archiveFromCurrentApp();
    }

    @Override
    public boolean setupUsingMaven(final Archive<?> givenArchive) throws Exception {
        final DependenciesContainer<?> archive = (DependenciesContainer<?>) givenArchive;

        Path pwd = Paths.get(System.getProperty("user.dir"));

        final Path classes = pwd.resolve("target").resolve("classes");

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

        final Path webapp = pwd.resolve("src").resolve("main").resolve("webapp");
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
