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
package org.wildfly.swarm;

import org.jboss.modules.MavenArtifactUtil;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bob McWhirter
 */
public class ArtifactManager {

    private WildFlySwarmDependenciesConf deps;

    public ArtifactManager(WildFlySwarmDependenciesConf deps) {
        this.deps = deps;
    }

    public ArtifactManager(InputStream in) throws IOException {
        this.deps = new WildFlySwarmDependenciesConf(in);
    }

    public ArtifactManager() throws IOException {
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(WildFlySwarmDependenciesConf.CLASSPATH_LOCATION);
        if ( in != null ) {
            this.deps = new WildFlySwarmDependenciesConf(in);
        }
    }

    public JavaArchive artifact(String gav) throws IOException, ModuleLoadException {
        File file = findFile(gav);
        if (file == null) {
            throw new RuntimeException("Artifact not found.");
        }
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, file.getName());
        new ZipImporterImpl(archive).importFrom(file);
        return archive;
    }

    public JavaArchive artifact(String gav, String asName) throws IOException, ModuleLoadException {
        File file = findFile(gav);
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, asName);
        new ZipImporterImpl(archive).importFrom(file);
        return archive;
    }

    public List<JavaArchive> allArtifacts() throws IOException {
        List<JavaArchive> archives = new ArrayList<>();

        if (this.deps != null ) {
            for (MavenArtifactDescriptor each : this.deps.getPrimaryDependencies()) {
                File artifact = MavenArtifactUtil.resolveJarArtifact(each.mscGav());
                JavaArchive archive = ShrinkWrap.create(JavaArchive.class, artifact.getName());
                new ZipImporterImpl(archive).importFrom(artifact);
                archives.add(archive);
            }
        } else {
            String classpath = System.getProperty("java.class.path");
            String javaHome = System.getProperty("java.home");
            Path pwd = Paths.get(System.getProperty("user.dir"));
            if (classpath != null) {
                String[] elements = classpath.split(File.pathSeparator);

                for (int i = 0; i < elements.length; ++i) {
                    if (!elements[i].startsWith(javaHome)) {
                        File artifact = new File(elements[i]);
                        if (artifact.isFile()) {
                            JavaArchive archive = ShrinkWrap.create(JavaArchive.class, artifact.getName());
                            new ZipImporterImpl(archive).importFrom(artifact);
                            archives.add(archive);
                        } else {
                            if (artifact.toPath().startsWith(pwd)) {
                                continue;
                            }

                            JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
                            Path basePath = artifact.toPath();
                            Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    //System.err.println(  basePath.relativize(file).toString() );
                                    archive.add(new FileAsset(file.toFile()), basePath.relativize(file).toString());
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                            archives.add(archive);
                        }
                    }
                }
            }

        }

        return archives;
    }

    private File findFile(String gav) throws IOException, ModuleLoadException {

        // groupId:artifactId
        // groupId:artifactId:version
        // groupId:artifactId:packaging:version
        // groupId:artifactId:packaging:version:classifier

        String[] parts = gav.split(":");

        if (parts.length < 2) {
            throw new RuntimeException("GAV must includes at least 2 segments");
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String packaging = "jar";
        String version = null;
        String classifier = "";

        if (parts.length == 3) {
            version = parts[2];
        }

        if (parts.length == 4) {
            packaging = parts[2];
            version = parts[3];
        }

        if (parts.length == 5) {
            packaging = parts[2];
            version = parts[3];
            classifier = parts[4];
        }

        if (version != null && (version.isEmpty() || version.equals("*"))) {
            version = null;
        }

        if (version == null) {
            version = determineVersionViaDependenciesConf(groupId, artifactId, packaging, classifier);
        }

        if (version == null) {
            version = determineVersionViaClasspath(groupId, artifactId, packaging, classifier);
        }

        if (version == null) {
            throw new RuntimeException("Unable to determine version number from 2-part GAV.  Try three!");
        }

        System.err.println( "found version: " + version );

        return MavenArtifactUtil.resolveArtifact(groupId + ":" + artifactId + ":" + version + (classifier == null ? "" : ":" + classifier), packaging);
    }

    String determineVersionViaDependenciesConf(String groupId, String artifactId, String packaging, String classifier) throws IOException {
        if ( this.deps != null ) {
            MavenArtifactDescriptor found = this.deps.find( groupId, artifactId, packaging, classifier );
            if ( found != null ) {
                return found.version();
            }
        }

        return null;
    }

    private String determineVersionViaClasspath(String groupId, String artifactId, String packaging, String classifier) {

        String regexp = ".*" + artifactId + "-(.+)" + (classifier.length() == 0 ? "" : "-" + classifier) + "." + packaging;
        Pattern pattern = Pattern.compile(regexp);

        String classpath = System.getProperty("java.class.path");
        String[] elements = classpath.split(File.pathSeparator);

        for (int i = 0; i < elements.length; ++i) {
            Matcher matcher = pattern.matcher(elements[i]);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        return null;
    }

}
