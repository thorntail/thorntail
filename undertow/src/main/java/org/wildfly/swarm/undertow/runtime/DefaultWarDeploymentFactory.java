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
package org.wildfly.swarm.undertow.runtime;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.wildfly.swarm.bootstrap.env.NativeDeploymentFactory;
import org.wildfly.swarm.container.runtime.deployments.DefaultJarDeploymentFactory;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.spi.api.DependenciesContainer;
import org.wildfly.swarm.spi.runtime.DefaultDeploymentFactory;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 * @author Heiko Braun
 */
@Singleton
public class DefaultWarDeploymentFactory extends DefaultDeploymentFactory {

    @Inject
    DefaultJarDeploymentFactory jarDeploymentFactory;

    NativeDeploymentFactory nativeDeploymentFactory;

    @Inject
    public DefaultWarDeploymentFactory(NativeDeploymentFactory nativeDeploymentFactory) {
        this.nativeDeploymentFactory = nativeDeploymentFactory;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getType() {
        return "war";
    }

    @Override
    public Archive create() throws Exception {
        Archive archive = getNativeDeployment();
        if (archive == null) {
            archive = createEmptyArchive();
            setupUsingMaven(archive);
        }
        //return archiveFromCurrentApp().staticContent();
        return archive.as(WARArchive.class).staticContent();
    }

    @Override
    public Archive createFromJar() throws Exception {
        Archive jar = this.jarDeploymentFactory.create();

        WARArchive war = ShrinkWrap.create(WARArchive.class, determineName());

        war.merge(jar, "/", (path) -> {
            String p = path.get();
            if (p.equals("/META-INF/jboss-deployment-structure.xml") || p.startsWith("/META-INF/services")) {
                return true;
            }
            return false;
        });
        war.merge(jar, "/WEB-INF/classes", (path) -> {
            String p = path.get();
            // because the filtered path is the final destination path...
            if (p.equals("/WEB-INF/classes/META-INF/jboss-deployment-structure.xml") || p.startsWith("/WEB-INF/classes/META-INF/services")) {
                return false;
            }
            return true;
        });

        boolean hasClasses = false;

        for (ArchivePath path : war.getContent().keySet()) {
            if (path.get().endsWith(".class")) {
                hasClasses = true;
                break;
            }
        }

        if (hasClasses) {
            war.addAllDependencies();
        }

        Map<ArchivePath, Node> content = war.getContent();

        for (ArchivePath archivePath : content.keySet()) {
            System.err.println( "synthetic war: " + archivePath );
        }

        return war;
    }

    protected Archive getNativeDeployment() throws IOException {
        return this.nativeDeploymentFactory.nativeDeployment();
    }

    protected Archive createEmptyArchive() {
        return this.nativeDeploymentFactory.createEmptyArchive( WARArchive.class, ".war" );
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
        return DefaultDeploymentFactory.determineName(".war");
    }

}
