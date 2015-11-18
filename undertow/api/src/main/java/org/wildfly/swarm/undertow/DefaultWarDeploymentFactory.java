/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.DefaultDeploymentFactory;
import org.wildfly.swarm.container.DependenciesContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class DefaultWarDeploymentFactory implements DefaultDeploymentFactory {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getType() {
        return "war";
    }

    @Override
    public Archive create(Container container) throws Exception {
        return archiveFromCurrentApp();
    }

    public static WARArchive archiveFromCurrentApp() throws Exception {
        WARArchive archive = ShrinkWrap.create(WARArchive.class, determineName());
        setup(archive);
        archive.addModule("org.wildfly.swarm.undertow", "runtime");
        return archive;
    }

    protected static String determineName() {
        String prop = System.getProperty( "wildfly.swarm.app.path" );
        if ( prop != null ) {
            File file = new File( prop );
            String name = file.getName();
            if ( name.endsWith( ".war" ) ) {
                return name;
            }
            return name + ".war";
        }

        prop = System.getProperty( "wildfly.swarm.app.artifact" );
        if ( prop != null ) {
            return prop;
        }

        return UUID.randomUUID().toString() + ".war";
    }

    protected static void setup(DependenciesContainer<?> archive) throws Exception {
        boolean result = setupUsingAppPath(archive) || setupUsingAppArtifact(archive) || setupUsingMaven(archive);
    }

    protected static boolean setupUsingAppPath(DependenciesContainer<?> archive) throws IOException {
        String appPath = System.getProperty("wildfly.swarm.app.path");

        if (appPath != null) {
            final Path path = Paths.get(System.getProperty("wildfly.swarm.app.path"));
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path simple = path.relativize(file);
                        archive.add(new FileAsset(file.toFile()), convertSeparators(simple));
                        return super.visitFile(file, attrs);
                    }
                });
            } else {
                ZipImporterImpl importer = new ZipImporterImpl(archive);
                importer.importFrom(new File(System.getProperty("wildfly.swarm.app.path")));
            }
            return true;
        }

        return false;
    }

    protected static boolean setupUsingAppArtifact(DependenciesContainer<?> archive) throws IOException {
        String appArtifact = System.getProperty("wildfly.swarm.app.artifact");

        if (appArtifact != null) {
            try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("_bootstrap/" + appArtifact)) {
                ZipImporterImpl importer = new ZipImporterImpl(archive);
                importer.importFrom(in);
            }
            return true;
        }

        return false;
    }

    protected static boolean setupUsingMaven(DependenciesContainer<?> archive) throws Exception {
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

    protected static String convertSeparators(Path path) {
        String convertedPath = path.toString();

        if (convertedPath.contains(File.separator)) {
            convertedPath = convertedPath.replace(File.separator, "/");
        }

        return convertedPath;
    }


}
