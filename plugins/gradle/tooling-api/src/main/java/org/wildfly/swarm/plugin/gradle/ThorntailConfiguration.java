/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wildfly.swarm.plugin.gradle;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.wildfly.swarm.fractions.FractionDescriptor;
import org.wildfly.swarm.tools.BuildTool;

/**
 * The {@code ThorntailConfiguration} type provides a view of the Thorntail specific configuration defined in the Gradle project.
 * Since this type is expected to be serialized over the wire, it inherently extends the Serializable interface.
 */
public interface ThorntailConfiguration extends Serializable {

    /**
     * Get the fraction detection mode for the Thorntail application.
     *
     * @return the fraction detection mode for this project.
     */
    BuildTool.FractionDetectionMode getFractionDetectionMode();

    /**
     * Set the fraction detection mode for this project.
     *
     * @param mode the fraction detection mode for this project.
     */
    void setFractionDetectionMode(BuildTool.FractionDetectionMode mode);

    /**
     * Get a collection of fractions that should be included along with the Thorntail application. This is useful for fractions
     * that cannot be detected or are user-provided. The fractions can be defined in the following formats,
     * <pre>
     *     group:name:version
     *     name:version
     *     name
     * </pre>
     *
     * If no "group" is provided, then "io.thorntail" is assumed. If no "version" is provided, then the version is taken
     * from the Thorntail Swarm BOM associated with the Gradle plugin.
     *
     * @return a collection of fractions that should be included along with the Thorntail application.
     */
    Set<String> getFractions();

    /**
     * Set a collection of fractions that should be included along with the Thorntail application. This is useful for fractions
     * that cannot be detected or are user-provided. The fractions can be defined in the following formats,
     * <pre>
     *     group:name:version
     *     name:version
     *     name
     * </pre>
     *
     * If no "group" is provided, then "io.thorntail" is assumed. If no "version" is provided, then the version is taken
     * from the Thorntail BOM associated with the Gradle plugin.
     *
     * @param fractions a collection of fractions that should be included along with the Thorntail application.
     */
    void setFractions(Set<String> fractions);

    /**
     * Get the paths to directories containing additional modules required by the Thorntail Application.
     *
     * @return the paths to directories containing additional modules required by the Thorntail Application.
     */
    Set<File> getModules();

    /**
     * Set the paths to directories containing additional modules required by the Thorntail Application.
     *
     * @param modules the paths to directories containing additional modules required by the Thorntail Application.
     */
    void setModules(Set<File> modules);

    /**
     * Should the build tool generate a hollow jar?
     *
     * @return true if the build tool should generate a hollow jar, false otherwise.
     */
    boolean isHollow();

    /**
     * Set whether or not the build tool should generate a hollow jar.
     *
     * @param hollow set to true if the build tool should generate a hollow jar, false otherwise.
     */
    void setHollow(boolean hollow);

    /**
     * Get the properties configured for the Thorntail application.
     *
     * @return the properties configured for the Thorntail application.
     */
    Properties getProperties();

    /**
     * Get the properties files that should be used for configuring the Thorntail application.
     *
     * @return the properties files that should be used for configuring the Thorntail application.
     */
    File getPropertiesFile();

    /**
     * Set the properties files that should be used for configuring the Thorntail application.
     *
     * @param file the properties files that should be used for configuring the Thorntail application.
     */
    void setPropertiesFile(File file);

    /**
     * Get the main class name that will be invoked when executing the Thorntail application.
     *
     * @return the main class name that will be invoked when executing the Thorntail application.
     * @deprecated Custom main() usage is intended to be deprecated in a future release and is no longer supported, please refer
     * to http://docs.wildfly-swarm.io for YAML configuration that replaces it.
     */
    @Deprecated
    String getMainClassName();

    /**
     * Set the main class name that will be invoked when executing the Thorntail application.
     *
     * @param className the main class name that will be invoked when executing the Thorntail application.
     * @deprecated Custom main() usage is intended to be deprecated in a future release and is no longer supported, please refer
     * to http://docs.wildfly-swarm.io for YAML configuration that replaces it.
     */
    @Deprecated
    void setMainClassName(String className);

    /**
     * Should the dependencies be bundled as part of the Thorntail application or fetched from the M2 repository?
     *
     * @return true if the dependencies should be bundled along with the uber jar, or false if the dependencies should be fetch
     * from the M2 repository instead.
     */
    boolean isBundleDependencies();

    /**
     * Set whether or not the dependencies should be bundled as part of the Thorntail application or fetched from the M2 repository.
     *
     * @param bundleDependencies set to true if the dependencies should be bundled along with the uber jar, or false if the
     *                           dependencies should be fetch from the M2 repository instead.
     */
    void setBundleDependencies(boolean bundleDependencies);

    /**
     * Should an executable script be included along with the archive?
     *
     * @return true if an executable script be included along with the archive, false otherwise.
     */
    boolean isIncludeExecutable();

    /**
     * Set whether or not an executable script should be included along with the archive.
     *
     * @param executable set to true if an executable script be included along with the archive, false otherwise.
     */
    void setIncludeExecutable(boolean executable);

    /**
     * Get the executable file that should be included along with the archive, if
     * {@link #isIncludeExecutable() include executable} has been set to true.
     *
     * @return the executable file that should be included along with the archive.
     */
    File getExecutableScript();

    /**
     * Set the executable file that should be included along with the archive, if
     * {@link #isIncludeExecutable() include executable} has been set to true.
     *
     * @param script the executable file that should be included along with the archive.
     */
    void setExecutableScript(File script);

    /**
     * @see #setDebugPort(Integer)
     */
    Integer getDebugPort();

    /**
     * When set, Thorntail executor will wait for debugger connection on given port.
     *
     * Used by "thorntail-run" and "thorntail-start" tasks.
     *
     * @param debugPort debugging port.
     */
    void setDebugPort(Integer debugPort);

    /**
     * @see #setStdoutFile(File)
     */
    File getStdoutFile();

    /**
     * File to log Thorntail executor standard output into.
     *
     * Used by "thorntail-run" and "thorntail-start" tasks.
     *
     * @param file log file for standard output.
     */
    void setStdoutFile(File file);

    /**
     * @see #setStderrFile(File)
     */
    File getStderrFile();

    /**
     * File to log Thorntail executor error output into.
     *
     * Used by "thorntail-run" and "thorntail-start" tasks.
     *
     * @param file log file for error output.
     */
    void setStderrFile(File file);

    /**
     * @see #setArguments(List)
     */
    List<String> getArguments();

    /**
     * Arguments to be given to the application main class when executing Thorntail process.
     *
     * Used by "thorntail-run" and "thorntail-start" tasks.
     *
     * @param arguments arguments for application main class.
     */
    void setArguments(List<String> arguments);

    /**
     * @see #setJvmArguments(List)
     */
    List<String> getJvmArguments();

    /**
     * JVM arguments ti be used when executing Thorntail process.
     *
     * Used by "thorntail-run" and "thorntail-start" tasks.
     *
     * @param arguments JVM arguments for Thorntail process.
     */
    void setJvmArguments(List<String> arguments);

    /**
     * @see #setUseUberJar(boolean)
     */
    boolean isUseUberJar();

    /**
     * Instead of adding compiled project classes and project dependencies on the classpath, the Thorntail executor will
     * directly execute the generated Uber-JAR.
     *
     * It is the responsibility of a user to ensure that the Uber-JAR exists and is up-to-date by running
     * "thorntail-package" task as necessary.
     *
     * Used by "thorntail-run" and "thorntail-start" tasks.
     *
     * @param useUberJar if true execute an Uber-JAR, if false compute and set the classpath.
     */
    void setUseUberJar(boolean useUberJar);

    /**
     * @see #setStartTimeout(int)
     */
    int getStartTimeout();

    /**
     * Sets a timeout for the Thorntail application process to start.
     *
     * Used by "thorntail-run" and "thorntail-start" tasks.
     *
     * @param startTimeout timeout in seconds.
     */
    void setStartTimeout(int startTimeout);

    /**
     * @see #setStopTimeout(int)
     */
    int getStopTimeout();

    /**
     * Sets a timeout for the Thorntail application process to stop, before it's forcibly destroyed.
     *
     * Used by "thorntail-run" and "thorntail-stop" tasks.
     *
     * @param stopTimeout timeout in seconds.
     */
    void setStopTimeout(int stopTimeout);

    /**
     * Environment variables that will be set for the Thorntail process.
     *
     * Used by "thorntail-run" and "thorntail-start" tasks.
     *
     * @return environment variables.
     */
    Properties getEnvironment();

    /**
     * @see #setEnvironmentFile(File)
     */
    File getEnvironmentFile();

    /**
     * Property file containing environment variables to be given to the Thorntail process.
     *
     * @param environmentFile property file with environment variables.
     */
    void setEnvironmentFile(File environmentFile);

    /**
     * Get the version of the plugin configured for the Gradle project. This method is used to determine the version when not
     * provided in the {@link #getFractions() fraction definitions}.
     *
     * @return the version of the plugin configured for the Gradle project
     */
    String getPluginVersion();

    /**
     * Get the dependency information for the current project. This method will return a map with keys being the direct
     * dependencies and the values being the key's resolved dependencies information. This method is useful primarily from
     * a tooling perspective and is not meant to be used by the consumers directly.
     */
    default Map<DependencyDescriptor, Set<DependencyDescriptor>> getDependencies() {
        return Collections.emptyMap();
    }

    /**
     * Get the test-scoped dependency information for the current project. This method will return a map with keys being the
     * direct dependencies and the values being the key's resolved dependencies. This method is useful primarily from a tooling
     * perspective and is not meant to be used by consumers directly.
     */
    default Map<DependencyDescriptor, Set<DependencyDescriptor>> getTestDependencies() {
        return Collections.emptyMap();
    }

    /**
     * Get the collection of declared fractions. This method will translate the fractions specified in the
     * {@link #getFractions() plugin configuration} section in to the appropriate {@code ArtifactSpec} instances.
     *
     * @return the collection of declared fractions.
     */
    default Set<DependencyDescriptor> getDeclaredFractions() {
        return getFractions().stream().map(entry -> {
            // Parse the entry in to an ArtifactSpec.
            String[] split = entry.split(":");
            String groupId = FractionDescriptor.THORNTAIL_GROUP_ID;
            String version = getPluginVersion();
            String artifactId;

            switch (split.length) {
                case 3:
                    // G:A:V available.
                    groupId = split[0];
                    artifactId = split[1];
                    version = split[2];
                    break;
                case 2:
                    // Check if we received G:A or A:V
                    if (FractionDescriptor.THORNTAIL_GROUP_ID.equals(split[0])) {
                        artifactId = split[1];
                    } else {
                        // A:V available.
                        artifactId = split[0];
                        version = split[1];
                    }
                    break;
                case 1:
                    // Artifact name only.
                    artifactId = split[0];
                    break;
                default:
                    throw new IllegalArgumentException("Unable to parse fraction definition: " + entry);
            }
            return new DefaultDependencyDescriptor("runtime", groupId, artifactId, version, "jar", null, null);
        }).collect(Collectors.toSet());
    }

}
