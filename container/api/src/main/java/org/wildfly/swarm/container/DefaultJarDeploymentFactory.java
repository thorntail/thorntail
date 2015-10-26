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
package org.wildfly.swarm.container;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;

/**
 * @author Bob McWhirter
 */
public class DefaultJarDeploymentFactory implements DefaultDeploymentFactory {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getType() {
        return "jar";
    }

    @Override
    public Archive create(Container container) throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, determineName());
        setup( archive );
        return archive;
    }

    protected String determineName() {
        String prop = System.getProperty( "wildfly.swarm.app.path" );
        if ( prop != null ) {
            File file = new File( prop );
            String name = file.getName();
            if ( name.endsWith( ".jar" ) ) {
                return name;
            }
            return name + ".jar";
        }

        prop = System.getProperty( "wildfly.swarm.app.artifact" );
        if ( prop != null ) {
            return prop;
        }

        return UUID.randomUUID().toString() + ".jar";
    }

    protected void setup(DependenciesContainer<?> archive) throws Exception {
        boolean result = setupUsingAppPath(archive) || setupUsingAppArtifact(archive) || setupUsingMaven(archive);
    }

    protected boolean setupUsingAppPath(DependenciesContainer<?> archive) throws IOException {
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

    protected boolean setupUsingAppArtifact(DependenciesContainer<?> archive) throws IOException {
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

    protected boolean setupUsingMaven(DependenciesContainer<?> archive) throws Exception {
        Path pwd = Paths.get(System.getProperty("user.dir"));

        final Path classes = pwd.resolve("target").resolve("classes");

        boolean success = false;

        if (Files.exists(classes)) {
            success = true;
            Files.walkFileTree(classes, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path simple = classes.relativize(file);
                    archive.add(new FileAsset(file.toFile()), convertSeparators(simple));
                    return super.visitFile(file, attrs);
                }
            });
        }

        archive.addAllDependencies();

        return success;
    }

    protected String convertSeparators(Path path) {
        String convertedPath = path.toString();

        if (convertedPath.contains(File.separator)) {
            convertedPath = convertedPath.replace(File.separator, "/");
        }

        return convertedPath;
    }


}
