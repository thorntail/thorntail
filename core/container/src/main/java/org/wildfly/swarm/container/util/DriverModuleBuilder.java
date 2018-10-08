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
package org.wildfly.swarm.container.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.wildfly.swarm.bootstrap.modules.DynamicModuleFinder;
import org.wildfly.swarm.bootstrap.util.JarFileManager;

/**
 * DriverModuleBuilder for applications that bring their own drivers.
 * Based on DriverInfo class.
 *
 * @author Scott Marlow
 */
public abstract class DriverModuleBuilder {

    private static final String FILE_PREFIX = "file:";
    private static final String JAR_FILE_PREFIX = "jar:file:";
    private final String name;
    private final String detectableClassName;
    private final String[] optionalClassNames;
    private final String[] driverModuleDependencies;

    private boolean installed;

    public DriverModuleBuilder(final String name,
                               final String detectableClassName,
                               final String[] optionalClassNames,
                               final String[] driverModuleDependencies) {
        this.name = name;
        this.detectableClassName = detectableClassName;
        this.optionalClassNames = optionalClassNames;
        this.driverModuleDependencies = driverModuleDependencies;
    }

    public String name() {
        return this.name;
    }

    public boolean detect(final String moduleName) {

        Messages.MESSAGES.attemptToAutoDetectDriver(this.name);

        File primaryJar = attemptDetection();

        if (primaryJar != null) {
            Set<File> optionalJars = findOptionalJars();

            optionalJars.add(primaryJar);
            DynamicModuleFinder.register(moduleName, (id, loader) -> {
                ModuleSpec.Builder builder = ModuleSpec.build(id);

                for (File eachJar : optionalJars) {
                    try {
                        JarFile jar = JarFileManager.INSTANCE.addJarFile(eachJar);
                        builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(
                                ResourceLoaders.createIterableJarResourceLoader(jar.getName(), jar)
                        ));
                    } catch (IOException e) {
                        Messages.MESSAGES.errorLoadingAutodetectedDriver(this.name, e);
                        return null;
                    }
                }

                for (String eachModuleIdentifier : driverModuleDependencies) {
                    builder.addDependency(DependencySpec.createModuleDependencySpec(eachModuleIdentifier));
                }
                builder.addDependency(DependencySpec.createLocalDependencySpec());

                return builder.create();
            });
            this.installed = true;
        }
        return this.installed;
    }

    private File attemptDetection() {
        return findLocationOfClass(this.detectableClassName);
    }

    private Set<File> findOptionalJars() {
        Set<File> optionalJars = new HashSet<>();

        if (this.optionalClassNames != null) {
            for (String each : this.optionalClassNames) {
                File file = findLocationOfClass(each);
                if (file != null) {
                    optionalJars.add(file);
                }
            }
        }

        return optionalJars;
    }

    private File findLocationOfClass(String className) {
        try {
            ClassLoader cl = Module.getBootModuleLoader().loadModule("thorntail.application").getClassLoader();
            File candidate = findLocationOfClass(cl, className);
            if (candidate == null) {
                candidate = findLocationOfClass(ClassLoader.getSystemClassLoader(), className);
            }

            return candidate;
        } catch (ModuleLoadException e) {
            // ignore
        } catch (IOException e) {
            Messages.MESSAGES.errorLoadingAutodetectedDriver(this.name, e);
        }

        return null;
    }

    private File findLocationOfClass(ClassLoader classLoader, String className) throws IOException {

        try {
            Class<?> driverClass = classLoader.loadClass(className);

            URL location = driverClass.getProtectionDomain().getCodeSource().getLocation();

            String locationStr = location.toExternalForm();
            if (locationStr.startsWith(JAR_FILE_PREFIX)) {
                locationStr = locationStr.substring(JAR_FILE_PREFIX.length());
            } else if (locationStr.startsWith(FILE_PREFIX)) {
                locationStr = locationStr.substring(FILE_PREFIX.length());
            }

            int bangLoc = locationStr.indexOf('!');
            if (bangLoc >= 0) {
                locationStr = locationStr.substring(0, bangLoc);
            }

            locationStr = getPlatformPath(locationStr);

            File locationFile = Paths.get(locationStr).toFile();

            return locationFile;
        } catch (ClassNotFoundException e) {
            // ignore;
        }

        return null;
    }

    private String getPlatformPath(String path) {
        if (!isWindows()) {
            return path;
        }

        URI uri = URI.create("file://" + path);
        return Paths.get(uri).toString();
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public boolean isInstalled() {
        return this.installed;
    }

    public String toString() {
        return "[DriverModuleBuilder: detectable=" + this.detectableClassName + "]";
    }

}
