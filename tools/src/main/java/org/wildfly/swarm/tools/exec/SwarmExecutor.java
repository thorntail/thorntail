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
package org.wildfly.swarm.tools.exec;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Bob McWhirter
 */
public class SwarmExecutor {

    public SwarmExecutor() {
        this.stdout = System.out;
        this.stderr = System.err;
        this.workingDirectory = Paths.get(System.getProperty("user.dir"));
    }

    public SwarmExecutor withStdoutFile(Path stdoutFile) {
        this.stdoutFile = stdoutFile;
        return this;
    }

    public SwarmExecutor withStderrFile(Path stderrFile) {
        this.stderrFile = stderrFile;
        return this;
    }

    public SwarmExecutor withDebug(Integer port) {
        this.debugPort = port;
        return this;
    }

    public SwarmExecutor withJava(Path java) {
        this.java = java;
        return this;
    }

    public Path getJava() {
        if (this.java == null) {
            return findJava();
        }
        return this.java;
    }

    public SwarmExecutor withDefaultSystemProperties() {
        Set<String> names = System.getProperties().stringPropertyNames();

        for (String name : names) {
            if (name.startsWith("jboss") || name.startsWith("swarm") || name.startsWith("wildfly") || name.startsWith("maven")) {
                String value = System.getProperty(name);
                this.properties.put(name, value);
            }
        }
        return this;
    }

    public SwarmExecutor withProperty(String name, String value) {
        this.properties.put(name, value);
        return this;
    }

    public SwarmExecutor withProperties(Properties props) {
        Set<String> names = props.stringPropertyNames();

        for (String name : names) {
            this.properties.put(name, props.getProperty(name));
        }

        return this;
    }

    public SwarmExecutor withEnvironment(String name, String value) {
        this.environment.put(name, value);
        return this;
    }

    public SwarmExecutor withEnvironment(Properties env) {
        Set<String> names = env.stringPropertyNames();

        for (String name : names) {
            this.environment.put(name, env.getProperty(name));
        }

        return this;
    }

    public SwarmExecutor withClasspathEntry(Path entry) {
        if (this.executable != null && this.executable instanceof ExecutableJar) {
            throw new RuntimeException("Cannot use a classpath with an executable jar");
        }
        this.classpath.add(entry);
        return this;
    }

    public SwarmExecutor withClassPathEntries(List<Path> entries) {
        if (this.executable != null && this.executable instanceof ExecutableJar) {
            throw new RuntimeException("Cannot use a classpath with an executable jar");
        }
        this.classpath.addAll(entries);
        return this;
    }

    public SwarmExecutor withModules(final List<Path> moduleDirs) {
        if (this.executable != null && this.executable instanceof ExecutableJar) {
            throw new RuntimeException("Cannot use modules with an executable jar");
        }
        final File moduleJar = new File(System.getProperty("java.io.tmpdir"),
                                        "swarm-module-overrides.jar");
        final JavaArchive moduleArchive = ShrinkWrap.create(JavaArchive.class);

        boolean modulesAdded = false;
        for (Path moduleDir : moduleDirs) {
            if (moduleDir.toFile().exists()) {
                moduleArchive.addAsResource(moduleDir.toFile(), "modules");
                modulesAdded = true;
            }
        }

        if (modulesAdded) {
            moduleArchive.as(ZipExporter.class)
                    .exportTo(moduleJar, true);

            withClasspathEntry(moduleJar.toPath());
        }

        return this;
    }

    public SwarmExecutor withExecutableJar(Path jar) {
        if (this.executable != null) {
            throw new RuntimeException("Executable already specified: " + this.executable);
        }

        if (!this.classpath.isEmpty()) {
            throw new RuntimeException("Cannot use executable jar with a classpath");
        }

        this.executable = new ExecutableJar(jar);
        return this;
    }

    public SwarmExecutor withMainClass(String className) {
        if (this.executable != null) {
            throw new RuntimeException("Executable already specified: " + this.executable);
        }
        this.executable = new MainClass(className);
        return this;
    }

    public SwarmExecutor withDefaultMainClass() {
        return withMainClass("org.wildfly.swarm.Swarm");
    }

    public SwarmExecutor withArgument(String arg) {
        this.arguments.add(arg);
        return this;
    }

    public SwarmExecutor withArguments(List<String> args) {
        this.arguments.addAll( args );
        return this;
    }

    public SwarmExecutor withJVMArgument(String arg) {
        this.jvmArguments.add(arg);
        return this;
    }

    public SwarmExecutor withJVMArguments(List<String> args) {
        this.jvmArguments.addAll(args);
        return this;
    }

    public SwarmExecutor withWorkingDirectory(Path workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public SwarmProcess execute() throws IOException {
        if (this.executable == null) {
            throw new RuntimeException("An executable jar or a main-class must be specified");
        }

        List<String> cli = new ArrayList<>();

        cli.add(getJava().toString());

        if (debugPort != null) {
            // see https://docs.oracle.com/javase/8/docs/technotes/guides/jpda/conninv.html
            cli.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + debugPort);
        }

        cli.addAll(this.jvmArguments);

        for (String name : this.properties.keySet()) {
            cli.add("-D" + name + "=" + this.properties.get(name));
        }

        if (!this.classpath.isEmpty()) {
            cli.add("-classpath");
            cli.add(String.join(File.pathSeparator,
                                this.classpath.stream()
                                        .sorted((o1, o2) -> {
                                            if (o1.toString().contains("weld-se-shaded")) {
                                                return -1;
                                            } else if (o2.toString().contains("weld-se-shaded")) {
                                                return 1;
                                            }
                                            return o1.compareTo(o2);
                                        })
                                        .map(e -> e.toString())
                                        .collect(Collectors.toList())));
        }

        cli.addAll(this.executable.toArguments());
        cli.addAll(this.arguments);


        final ProcessBuilder processBuilder = new ProcessBuilder(cli)
                .directory(this.workingDirectory.toFile());
        processBuilder.environment().putAll(environment);
        Process process = processBuilder.start();

        return new SwarmProcess(
                process,
                this.stdout, this.stdoutFile,
                this.stderr, this.stderrFile);
    }

    protected Path findJava() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new RuntimeException("unable to locate java binary");
        }

        Path binDir = FileSystems.getDefault().getPath(javaHome, "bin");

        Path java = binDir.resolve("java.exe");
        if (java.toFile().exists()) {
            return java;
        }

        java = binDir.resolve("java");
        if (java.toFile().exists()) {
            return java;
        }

        throw new RuntimeException("unable to locate java binary");
    }

    private final OutputStream stdout;

    private final OutputStream stderr;

    private Path stdoutFile;

    private Path stderrFile;

    private Path java;

    private Map<String, String> properties = new HashMap<>();

    private Map<String, String> environment = new HashMap<>();

    private List<Path> classpath = new ArrayList<>();

    private List<String> jvmArguments = new ArrayList<>();

    private List<String> arguments = new ArrayList<>();

    private Executable executable;

    private Path workingDirectory;

    private Integer debugPort;

}
