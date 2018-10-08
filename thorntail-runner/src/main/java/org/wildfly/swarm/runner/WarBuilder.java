/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 8/1/18
 */
public class WarBuilder {

    public static File build(List<String> classesDirs, List<File> classpathJars) throws IOException {
        File war = File.createTempFile("thorntail-user-war", ".war");
        war.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(war);
             ZipOutputStream out = new ZipOutputStream(fos)) {

            WarBuilder builder = new WarBuilder(out, classesDirs, classpathJars);
            builder.build();
        }
        System.out.println("built " + war.getAbsolutePath());
        return war;
    }

    private WarBuilder(ZipOutputStream output, List<String> classesDirs, List<File> jars) {
        this.output = output;
        this.classesDirs = classesDirs;
        this.jars = jars;
    }

    private void build() {
        classesDirs.forEach(this::addClassesToWar);
        addWebAppResourcesToWar();
        jars.stream().forEach(this::addJarToWar);
    }


    private void addClassesToWar(String directory) {
        File classesDirectory = new File(directory);
        if (!classesDirectory.isDirectory()) {
            throw new RuntimeException("Invalid classes directory on classpath: " + directory);
        }
        addClassesToWar(classesDirectory);
    }

    private void addWebAppResourcesToWar() {
        try {
            Path webappPath = getWebAppLocation();
            if (!webappPath.toFile().exists()) {
                return;
            }
            Files.walk(webappPath)
                    .forEach(this::addWebappResourceToWar);
        } catch (IOException e) {
            throw new RuntimeException("Unable to get webapp dir");
        }
    }

    private Path getWebAppLocation() {
        Path webappPath;
        // TODO: if maven project - check in the pom if it's not different than default
        String webappLocationProperty = System.getProperty("thorntail.runner.webapp-location");

        if (webappLocationProperty != null) {
            webappPath = Paths.get(webappLocationProperty);
            if (!webappPath.toFile().exists()) {
                // user provided a location for webapp dir but it's invalid
                System.err.println("Invalid web app location directory provided: " + webappLocationProperty);
                System.exit(1);
            }
        } else {
            webappPath = Paths.get("src", "main", "webapp");
        }
        return webappPath;
    }

    private void addJarToWar(File file) {
        String jarName = file.getName();
        try {
            writeFileToZip(file, "WEB-INF/lib/" + jarName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to add jar " + file.getAbsolutePath() + " to war", e);
        }
    }

    private void addWebappResourceToWar(Path path) {
        File file = path.toFile();
        if (file.isFile()) {
            try {
                String projectDir = Paths.get("src", "main", "webapp").toFile().getAbsolutePath();

                String fileName = file.getAbsolutePath().replace(projectDir, "");
                writeFileToZip(file, fileName);
            } catch (IOException e) {
                throw new RuntimeException("Unable to add file: " + path.toAbsolutePath() + "  from webapp to the war", e);
            }
        }
    }

    private void addClassesToWar(File classesDirectory) {
        try {
            Files.walk(classesDirectory.toPath())
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .forEach(file -> addClassToWar(file, classesDirectory));
        } catch (IOException e) {
            throw new RuntimeException("Failed to add classes to war", e);
        }
    }

    private void addClassToWar(File file, File classesDirectory) {
        try {
            String name = getRelativePath(file, classesDirectory.getAbsolutePath());
            name = "/WEB-INF/classes/" + name;
            writeFileToZip(file, name);
        } catch (IOException e) {
            throw new RuntimeException("Failed to add file " + file.getAbsolutePath() + " to war", e);
        }
    }

    private String getRelativePath(File file, String parentPath) {
        String relativePath = file.getAbsolutePath().substring(parentPath.length());
        String separator = FileSystems.getDefault().getSeparator();
        if (relativePath.startsWith(separator)) {
            relativePath = relativePath.substring(separator.length());
        }
        return relativePath;
    }

    private void writeFileToZip(File file, String name) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        output.putNextEntry(entry);
        try (FileInputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) >= 0) {
                output.write(buffer, 0, length);
            }
        }
        output.closeEntry();
    }

    private final ZipOutputStream output;
    private final List<String> classesDirs;
    private final List<File> jars;
}
