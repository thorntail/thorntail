/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.runner;

import org.wildfly.swarm.jdk.specific.ClassLoaders;
import org.wildfly.swarm.runner.cache.RunnerCacheConstants;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * The class to execute when running Thorntail in the IDE.
 *
 * During execution, it resolves project's dependencies to ${user.home}
 *
 * It first generates an uber-jar and then executes it.
 * The jar is generated from the IDE classpath by taking the Thorntail dependencies, generating an uber-jar in the similar way
 * the thorntail-maven-plugin does and removing all the transitive dependencies of the Thorntail elements from WEB-INF/lib
 * in the internal WAR.
 *
 * The following options are available to control the execution:
 * <ul>
 *     <li>
 *         <b>thorntail.runner.preserve-jar</b> - keep the generated uber-jar when the application is stopped; might be useful for debugging
 *     </li>
 *     <li>
 *         <b>thorntail.runner.user-dependencies</b> - a comma-separated list of groupId:artifactId:version[:classifier] of artifacts
 *         to keep in WEB-INF/lib even though they are dependencies of Swarm elements
 *     </li>
 *     <li>
 *         <b>thorntail.runner.main-class</b> - user's main class replacing the default org.wildfly.swarm.Swarm class.
 *         Please note that using custom main class is discouraged.
 *     </li>
 *     <li>
 *         <b>thorntail.runner.webapp-location</b> - by default, Runner expects webapp in `src/main/webapp`. This property
 *         can be used to point to a different location of the webapp directory
 *     </li>
 *     <li>
 *         <b>thorntail.runner.repositories</b> - additional maven repositories to look for artifacts.
 *         By default Runner searches for artifacts in Maven Central and repository.jboss.org.
 *         Expects a comma separated list of repositoryUrl[#username#password].
 *     </li>
 *     <li>
 *         <b>thorntail.runner.local-repository</b> - location of the local repository.
 *         By default, Runner will use ${user.home}/.m2/repository to store any resolved artifacts.
 *         This is the default Maven location. This property let's you choose a different location.
 *     </li>
 * </ul>
 *
 */
public class Runner {

    private static final String PRESERVE_JAR = "thorntail.runner.preserve-jar";

    private Runner() {
    }

    public static void main(String[] args) throws Exception {
        System.out.printf("Starting Thorntail Runner. Runner caches will be stored in %s\n", RunnerCacheConstants.CACHE_STORAGE_DIR);
        URLClassLoader loader = createClassLoader();
        run((Object) args, loader);
    }

    private static void run(Object args, URLClassLoader loader) throws Exception {
        callWithClassloader(loader,
                "org.wildfly.swarm.bootstrap.Main",
                "main",
                new Class<?>[]{String[].class},
                args);
    }

    private static <T> T callWithClassloader(ClassLoader loader,
                                             String className,
                                             String methodName,
                                             Class<?>[] argumentTypes,
                                             Object... arguments) throws Exception {
        Thread.currentThread().setContextClassLoader(loader);
        Class<?> aClass = loader.loadClass(className);
        Method method = aClass.getMethod(methodName, argumentTypes);
        return (T) method.invoke(null, arguments);
    }

    private static URLClassLoader createClassLoader() throws Exception {
        File fatJar = File.createTempFile("thorntail-user-app", ".jar");
        buildJar(fatJar);


        if (System.getProperties().getProperty(PRESERVE_JAR) == null) {
            System.out.println("Built " + fatJar.getAbsolutePath() + ", the file will be deleted on shutdown. To keep it, use -D" + PRESERVE_JAR);
            fatJar.deleteOnExit();
        }

        URL jarUrl = fatJar.toURI().toURL();
        return new URLClassLoader(new URL[]{jarUrl}, ClassLoaders.getPlatformClassLoader());
    }

    private static void buildJar(File fatJar) throws IOException, InterruptedException {
        String classpath = System.getProperty("java.class.path");

        List<String> command = buildCommand(fatJar);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.environment().put("CLASSPATH", classpath);

        Process fatJarBuilder = processBuilder
                .inheritIO()
                .start();


        int exitCode = fatJarBuilder.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to generate the uber jar.");
        }
    }

    /*
    builds a command like:
    /my/path/to/java -Dall -Dsystem=properties JarBuilderClassName pathToTargetJar
     */
    private static List<String> buildCommand(File fatJar) {
        List<String> command = new ArrayList<>(Collections.singleton(javaCommand()));

        command.addAll(properties());
        command.addAll(asList(
                FatJarBuilder.class.getCanonicalName(),
                fatJar.getAbsolutePath()
        ));
        return command;
    }

    private static Collection<String> properties() {
        return System.getProperties()
                .entrySet()
                .stream()
                .map(Runner::propertyToString)
                .collect(Collectors.toList());
    }

    private static String propertyToString(Map.Entry<Object, Object> property) {
        return property.getValue() == null
                ? format("-D%s", property.getKey())
                : format("-D%s=%s", property.getKey(), property.getValue());
    }

    private static String javaCommand() {
        Path javaBinPath = Paths.get(System.getProperty("java.home"), "bin");
        File javaExecutable = javaBinPath.resolve("java").toFile();
        if (!javaExecutable.exists()) {
            javaExecutable = javaBinPath.resolve("java.exe").toFile();
        }
        return javaExecutable.getAbsolutePath();
    }
}