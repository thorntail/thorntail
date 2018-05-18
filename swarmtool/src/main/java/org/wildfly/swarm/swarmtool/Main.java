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
package org.wildfly.swarm.swarmtool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenChecksumPolicy;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;
import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.fractions.FractionDescriptor;
import org.wildfly.swarm.fractions.FractionList;
import org.wildfly.swarm.fractions.PropertiesUtil;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.DeclaredDependencies;

import static java.util.Arrays.asList;

public class Main {

    protected Main() {
    }

    private static final OptionParser OPT_PARSER = new OptionParser();

    private static final OptionSpec<Void> HOLLOW_OPT =
            OPT_PARSER.accepts("hollow", "creates a swarm jar containing only the fractions");

    private static final OptionSpec<Void> HELP_OPT =
            OPT_PARSER.acceptsAll(asList("h", "help"), "print help and exit")
                    .forHelp();

    private static final OptionSpec<Void> VERSION_OPT =
            OPT_PARSER.acceptsAll(asList("v", "version"), "print version and exit")
                    .forHelp();

    private static final OptionSpec<Void> DISABLE_AUTO_DETECT_OPT =
            OPT_PARSER.accepts("no-fraction-detect", "disable auto fraction detection");

    private static final OptionSpec<Void> DISABLE_BUNDLE_DEPS_OPT =
            OPT_PARSER.accepts("no-bundle-deps", "disable bundling of dependencies");

    private static final OptionSpec<Void> EXECUTABLE_OPT =
            OPT_PARSER.accepts("executable", "make the swarm jar executable");

    private static final OptionSpec<Void> DEBUG_LOGGING =
            OPT_PARSER.accepts("debug", "enable debug logging");

    private static final OptionSpec<String> FRACTIONS_OPT =
            OPT_PARSER.acceptsAll(asList("f", "fractions"), "swarm fractions to include")
                    .withRequiredArg()
                    .ofType(String.class)
                    .withValuesSeparatedBy(',')
                    .describedAs("undertow,jaxrs,...");

    private static final OptionSpec<String> DEPENDENCIES_OPT =
            OPT_PARSER.acceptsAll(asList("d", "dependencies"), "Maven coordinates (groupId:artifactId:version) of dependencies to include")
                    .withRequiredArg()
                    .ofType(String.class)
                    .withValuesSeparatedBy(",")
                    .describedAs("gav1,gav2,...");

    private static final OptionSpec<String> REPOS_OPT =
            OPT_PARSER.accepts("repos", "additional maven repos to resolve against")
                    .withRequiredArg()
                    .ofType(String.class)
                    .withValuesSeparatedBy(',')
                    .describedAs("url1,url2,...");

    private static final OptionSpec<String> OUTPUT_DIR_OPT =
            OPT_PARSER.acceptsAll(asList("o", "output-dir"), "directory where the final jar will be written")
                    .withRequiredArg()
                    .ofType(String.class)
                    .defaultsTo(".")
                    .describedAs("path");

    private static final OptionSpec<String> MAIN_OPT =
            OPT_PARSER.accepts("main", "The name of the custom main class")
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("main-class");

    private static final OptionSpec<String> MODULES_OPT =
            OPT_PARSER.accepts("modules", "A list of JBoss Modules module dirs to include")
                    .withRequiredArg()
                    .ofType(String.class)
                    .withValuesSeparatedBy(',')
                    .describedAs("module-dir1,module-dir2,...");

    private static final OptionSpec<String> NAME_OPT =
            OPT_PARSER.acceptsAll(asList("n", "name"), "The name of the final jar sans the -swarm.jar suffix (default: <source name>)")
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("jar-name");

    private static final OptionSpec<File> SOURCE_OPT =
            OPT_PARSER.nonOptions("The source artifact")
                    .ofType(File.class);

    private static final OptionSpec<String> SYSPROPS_OPT =
            OPT_PARSER.accepts("D", "system property (overrides entry in --property-file)")
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("key=value");

    private static final OptionSpec<File> SYSPROPS_FILE_OPT =
            OPT_PARSER.accepts("property-file", "system properties")
                    .withRequiredArg()
                    .ofType(File.class)
                    .describedAs("system properties file");

    protected static final String VERSION;

    static {
        try {
            VERSION = PropertiesUtil
                    .loadProperties(Main.class.getClassLoader()
                                            .getResourceAsStream("org/wildfly/swarm/swarmtool/version.properties"))
                    .getProperty("version");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load version.properties", e);
        }
    }

    public static void main(final String[] args) throws Exception {
        try {
            generateSwarmJar(args);
        } catch (ExitException e) {
            final String msg = e.getMessage();
            if (msg != null) {
                System.err.println(msg);
            }

            if (e.printHelp) {
                if (msg != null) {
                    System.err.println();
                }
                System.err.println(String.format("Usage: %s <options> artifact-path\n", executableName()));
                try {
                    OPT_PARSER.printHelpOn(System.err);
                } catch (IOException ignored) {
                }
            }

            System.exit(e.status);
        }
    }

    protected static String executableName() {
        String name = System.getenv("SWARMTOOL_NAME");
        if (name == null) {
            name = "java -jar swarmtool-standalone.jar";
        }

        return name;
    }

    protected static File generateSwarmJar(final String[] args) throws Exception {
        OptionSet foundOptions = null;

        try {
            foundOptions = OPT_PARSER.parse(args);
        } catch (OptionException e) {
            exit(e.getMessage(), true);
        }

        if (foundOptions.has(HELP_OPT)) {
            exit(null, 0, true);
        }

        if (foundOptions.has(VERSION_OPT)) {
            exit("swarmtool v" + VERSION, 0);
        }

        final List<File> nonOptArgs = foundOptions.valuesOf(SOURCE_OPT);
        if (nonOptArgs.isEmpty()) {
            exit("No source artifact specified.", true);
        }
        if (nonOptArgs.size() > 1) {
            exit("Too many source artifacts provided (" + nonOptArgs + ")", true);
        }

        final File source = nonOptArgs.get(0);
        if (!source.exists()) {
            exit("File " + source.getAbsolutePath() + " does not exist.");
        }

        final Properties properties = new Properties();
        if (foundOptions.has(SYSPROPS_FILE_OPT)) {
            try (InputStream in = new FileInputStream(foundOptions.valueOf(SYSPROPS_FILE_OPT))) {
                properties.load(in);
            }
        }
        foundOptions.valuesOf(SYSPROPS_OPT)
                .forEach(prop -> {
                    final String[] parts = prop.split("=");
                    properties.put(parts[0], parts[1]);
                });

        final DeclaredDependencies dependencies = new DeclaredDependencies();
        foundOptions.valuesOf(DEPENDENCIES_OPT).stream()
                .map(DeclaredDependencies::createSpec)
                .forEach(dependencies::add);

        final String[] parts = source.getName().split("\\.(?=[^\\.]+$)");
        final String baseName = parts[0];
        final String type = parts[1] == null ? "jar" : parts[1];
        final String jarName = foundOptions.has(NAME_OPT) ? foundOptions.valueOf(NAME_OPT) : baseName;
        final String outDir = new File(foundOptions.valueOf(OUTPUT_DIR_OPT)).getCanonicalPath();
        final String suffix = foundOptions.has(HOLLOW_OPT) ? "-hollow-swarm" : "-swarm";
        final BuildTool tool = new BuildTool(getResolvingHelper(foundOptions.valuesOf(REPOS_OPT)))
                .projectArtifact("", baseName, "", type, source)
                .declaredDependencies(dependencies)
                .fractionDetectionMode(foundOptions.has(DISABLE_AUTO_DETECT_OPT) ?
                                               BuildTool.FractionDetectionMode.never :
                                               BuildTool.FractionDetectionMode.force)
                .bundleDependencies(!foundOptions.has(DISABLE_BUNDLE_DEPS_OPT))
                .executable(foundOptions.has(EXECUTABLE_OPT))
                .properties(properties)
                .hollow(foundOptions.has(HOLLOW_OPT));

        if (foundOptions.has(MAIN_OPT)) {
            tool.mainClass(foundOptions.valueOf(MAIN_OPT));
        }
        if (foundOptions.has(MODULES_OPT)) {
            tool.additionalModules(foundOptions.valuesOf(MODULES_OPT));
        }
        if (foundOptions.has(DEBUG_LOGGING)) {
            tool.logger(BuildTool.STD_LOGGER_WITH_DEBUG);
        }

        addSwarmFractions(tool, foundOptions.valuesOf(FRACTIONS_OPT));

        System.err.println(String.format("Building %s/%s%s.jar", outDir, jarName, suffix));
        return tool.build(jarName, Paths.get(outDir));
    }

    private static void exit(String message) {
        exit(message, 1);
    }

    private static void exit(String message, boolean printHelp) {
        exit(message, 1, printHelp);
    }

    private static void exit(String message, int code) {
        exit(message, code, false);
    }

    private static void exit(String message, int code, boolean printHelp) {
        throw new ExitException(code, printHelp, message);
    }

    private static ArtifactResolvingHelper getResolvingHelper(final List<String> repos) {
        final ConfigurableMavenResolverSystem resolver = Maven.configureResolver()
                .withMavenCentralRepo(true)
                .withRemoteRepo(MavenRemoteRepositories.createRemoteRepository("jboss-public-repository-group",
                                                                               "https://repository.jboss.org/nexus/content/groups/public/",
                                                                               "default")
                                        .setChecksumPolicy(MavenChecksumPolicy.CHECKSUM_POLICY_IGNORE)
                                        .setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER));

        repos.forEach(r -> resolver.withRemoteRepo(MavenRemoteRepositories.createRemoteRepository(r, r, "default")
                                                           .setChecksumPolicy(MavenChecksumPolicy.CHECKSUM_POLICY_IGNORE)
                                                           .setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER)));

        return new ShrinkwrapArtifactResolvingHelper(resolver);
    }

    private static void addSwarmFractions(BuildTool tool, final List<String> deps) {
        deps.stream().map(f -> f.split(":"))
                .map(parts -> {
                    switch (parts.length) {
                        case 1:
                            final FractionDescriptor desc = FractionList.get()
                                    .getFractionDescriptor("io.thorntail", parts[0]);
                            if (desc != null) {

                                return desc;
                            } else {
                                System.err.println("Warning: Unknown fraction: " + parts[0]);

                                return null;
                            }
                        case 2:

                            return new FractionDescriptor("io.thorntail", parts[0], parts[1]);
                        case 3:

                            return new FractionDescriptor(parts[0], parts[1], parts[2]);
                        default:
                            System.err.println("Warning: Invalid fraction specifier: " + String.join(":", parts));

                            return null;
                    }
                })
                .filter(f -> f != null)
                .forEach(f -> tool.fraction(ArtifactSpec.fromFractionDescriptor(f)));
    }

    static class ExitException extends RuntimeException {
        public int status;

        public boolean printHelp;

        ExitException(final int status, final boolean printHelp, final String message) {
            super(message);
            this.printHelp = printHelp;
            this.status = status;
        }
    }
}
