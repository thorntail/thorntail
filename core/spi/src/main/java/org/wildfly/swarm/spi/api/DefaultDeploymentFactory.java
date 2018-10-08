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
package org.wildfly.swarm.spi.api;

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
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;

/** Abstract factory for creating default deployments automatically.
 *
 * @apiNote Used by {@code Fraction} authors.
 *
 * @author Bob McWhirter
 */
public abstract class DefaultDeploymentFactory {

    public static String APP_NAME = "thorntail.app.name";
    public static String APP_PATH = "thorntail.app.path";
    public static String APP_ARTIFACT = "thorntail.app.artifact";

    public abstract int getPriority();

    public abstract String getType();

    public abstract Archive create() throws Exception;

    public boolean setup(Archive<?> archive) throws Exception {
        return setupUsingAppPath(archive) ||
                setupUsingAppArtifact(archive) ||
                setupUsingMaven(archive);
    }

    protected static String determineName(final String suffix) {
        String prop = System.getProperty(APP_NAME);
        if (prop != null) {
            return prop;
        }

        prop = System.getProperty(APP_PATH);
        if (prop != null) {
            final File file = new File(prop);
            final String name = file.getName();
            if (name.endsWith(suffix)) {

                return name;
            }

            return name + suffix;
        }

        prop = System.getProperty(APP_ARTIFACT);
        if (prop != null) {
            return prop;
        }

        return UUID.randomUUID().toString() + suffix;
    }

    protected String convertSeparators(Path path) {
        String convertedPath = path.toString();

        if (convertedPath.contains(File.separator)) {
            convertedPath = convertedPath.replace(File.separator, "/");
        }

        return convertedPath;
    }

    protected boolean setupUsingAppPath(Archive<?> archive) throws IOException {
        final String appPath = System.getProperty(APP_PATH);

        if (appPath != null) {
            final Path path = Paths.get(appPath);
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
                archive.as(ZipImporter.class)
                        .importFrom(path.toFile());
            }
            return true;
        }

        return false;
    }

    protected boolean setupUsingAppArtifact(Archive<?> archive) throws IOException {
        final String appArtifact = System.getProperty(APP_ARTIFACT);

        if (appArtifact != null) {
            try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("_bootstrap/" + appArtifact)) {
                archive.as(ZipImporter.class)
                        .importFrom(in);
            }
            return true;
        }

        return false;
    }

    protected abstract boolean setupUsingMaven(Archive<?> archive) throws Exception;
}
