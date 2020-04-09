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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
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
        List<String> warDirs = classesDirs.stream()
                .filter(this::isWar)
                .collect(Collectors.toList());

        classesDirs.stream()
                .filter(d -> !isWar(d))
                .map(this::buildJar)
                .forEach(jars::add);

        warDirs.forEach(this::addClassesToWar);
        addWebAppResourcesToWar();

        jars.forEach(this::addJarToWar);
    }

    private File buildJar(String classesDir) {
        try {
            File jar = File.createTempFile("thorntail-user-jar", ".jar");
            jar.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(jar);
                 ZipOutputStream out = new ZipOutputStream(fos)) {
                addClassesToZip(out, "", new File(classesDir));
            }
            return jar;
        } catch (Exception any) {
            throw new RuntimeException("Unable to build jar from directory: " + classesDir, any);
        }
    }

    // the assumption is that the Runner is invoked in the WAR module's directory
    private boolean isWar(String path) {
        String currentDir = Paths.get(".").toAbsolutePath().normalize().toString();
        String classesDirPath = Paths.get(path).toAbsolutePath().normalize().toString();
        return classesDirPath.startsWith(currentDir);
    }


    private void addClassesToWar(String directory) {
        File classesDirectory = new File(directory);
        if (!classesDirectory.isDirectory()) {
            throw new RuntimeException("Invalid classes directory on classpath: " + directory);
        }
        addClassesToZip(output, "/WEB-INF/classes/", classesDirectory);
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
            writeFileToZip(output, file, "WEB-INF/lib/" + jarName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to add jar " + file.getAbsolutePath() + " to war", e);
        }
    }

    private void addWebappResourceToWar(Path path) {
        File file = path.toFile();
        if (file.isFile()) {
            try {
                String projectDir = System.getProperty("thorntail.runner.webapp-location");
                if (projectDir != null) {
                    projectDir = Paths.get(projectDir).toFile().getAbsolutePath();
                    if (file.getAbsolutePath().contains("WEB-INF" + File.separator + "classes")) { // Ignore classes.
                        return;
                    }
                } else {
                    projectDir = Paths.get("src", "main", "webapp").toFile().getAbsolutePath();
                }
                String fileName = file.getAbsolutePath().replace(projectDir, "");
                writeFileToZip(output, file, fileName);
            } catch (IOException e) {
                throw new RuntimeException("Unable to add file: " + path.toAbsolutePath() + "  from webapp to the war", e);
            }
        }
    }

    private void addClassesToZip(ZipOutputStream output, String prefix, File classesDirectory) {
        try {
            Files.walk(classesDirectory.toPath())
                    .map(Path::toFile)
                    .forEach(file -> addClassToZip(output, prefix, file, classesDirectory));
        } catch (IOException e) {
            throw new RuntimeException("Failed to add classes to war", e);
        }
    }

    private void addClassToZip(ZipOutputStream output, String prefix, File file, File classesDirectory) {
        URI base = classesDirectory.toURI();
        try {
            String name = base.relativize(file.toURI()).getPath();
            name = prefix + name;
            if (file.isDirectory()) {
              name = name.endsWith("/") ? name : name + "/";
              output.putNextEntry(new ZipEntry(name));
              output.closeEntry();
            } else {
              writeFileToZip(output, file, name);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to add file " + file.getAbsolutePath() + " to war", e);
        }
    }

    private static void writeFileToZip(ZipOutputStream output,
                                       File file,
                                       String name) throws IOException {
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
