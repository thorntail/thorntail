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
package org.wildfly.swarm.plugin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bob McWhirter
 */
@Mojo(name = "analyze",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Execute(phase = LifecyclePhase.PACKAGE)
public class AnalyzeMojo extends AbstractMojo {

    @Parameter(alias = "gav", defaultValue = "${gav}", required = true)
    private String gav;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    private Path dir;
    private Path modulesDir;

    private Graph graph = new Graph();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Analyzing for " + this.gav);

        this.dir = Paths.get(this.projectBuildDir, "wildfly-swarm-archive");
        this.modulesDir = this.dir.resolve("modules");

        try {
            walkModulesDir();
            walkDependencies();
        } catch (IOException e) {
            throw new MojoFailureException("Unable to inspect modules/ dir", e);
        }

        Graph.Artifact artifact = this.graph.getClosestArtifact(this.gav);
        if ( artifact == null ) {
            throw new MojoFailureException( "Unable to find artifact: " + this.gav );
        }

        DumpGraphVisitor visitor = new DumpGraphVisitor();
        artifact.accept( visitor );

    }

    protected void walkModulesDir() throws IOException {
        Files.walkFileTree(this.modulesDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().equals("module.xml")) {
                    Path dir = file.getParent();
                    Path relative = modulesDir.relativize(dir);
                    String slot = relative.getFileName().toString();
                    String module = relative.getParent().toString().replace(File.separatorChar, '.');
                    analyzeModuleXml(module, slot, new FileInputStream(file.toFile()));
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    protected void walkDependencies() throws IOException {
        Set<Artifact> deps = this.project.getArtifacts();

        for (Artifact each : deps) {
            walkDependency(each);
        }
    }

    protected void walkDependency(Artifact artifact) throws IOException {

        if (artifact.getFile() != null && artifact.getType().equals("jar")) {

            try (JarFile jar = new JarFile(artifact.getFile())) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith("modules/") && entry.getName().endsWith("module.xml")) {
                        String[] parts = entry.getName().split("/");

                        String slot = parts[parts.length - 2];
                        String module = null;
                        for (int i = 1; i < parts.length - 2; ++i) {
                            if (module != null) {
                                module = module + ".";
                                ;
                            } else {
                                module = "";
                            }
                            module = module + parts[i];
                        }

                        analyzeModuleXml(module, slot, jar.getInputStream(entry));
                    }
                }
            }
        }

    }

    private static Pattern ARTIFACT = Pattern.compile("<artifact name=\"([^\"]+)\"");

    private static Pattern MODULE = Pattern.compile("<module name=\"([^\"]+)\".*(slot=\"[^\"]+\")?");

    protected void analyzeModuleXml(String module, String slot, InputStream in) throws IOException {
        Graph.Module curModule = this.graph.getModule(module, slot);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = null;

                matcher = ARTIFACT.matcher(line);
                if (matcher.find()) {
                    String gav = matcher.group(1);
                    String parts[] = gav.split(":");

                    String groupId = parts[0];
                    String artifactId = parts[1];
                    String version = parts[2];
                    String classifier = null;
                    if ( parts.length >= 4 ) {
                        classifier = parts[3];
                    }

                    curModule.addArtifact( this.graph.getArtifact( groupId, artifactId, version, classifier ));
                } else {
                    matcher = MODULE.matcher(line);
                    if (matcher.find()) {
                        String depModule = matcher.group(1);
                        String depSlot = "main";
                        if (matcher.groupCount() >= 3) {
                            depSlot = matcher.group(3);
                        }
                        curModule.addDependency(this.graph.getModule(depModule, depSlot));
                    }
                }
            }
        }
    }

}
