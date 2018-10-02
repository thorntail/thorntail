/*
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
package org.wildfly.swarm.internal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Heiko Braun
 * @since 02/08/16
 */
public class MavenFileSystemLayout extends FileSystemLayout {

    MavenFileSystemLayout(String root) {
        super(root);
    }

    @Override
    public Path resolveBuildClassesDir() {
        return rootPath.resolve(TARGET).resolve(CLASSES);
    }

    @Override
    public Path resolveBuildResourcesDir() {
        // maven actually keeps them under target/classes
        return resolveBuildClassesDir();
    }

    @Override

    public Path resolveSrcWebAppDir() {
        return rootPath.resolve(SRC).resolve(MAIN).resolve(WEBAPP);
    }

    @Override
    public String determinePackagingType() {
        String type = null;
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(rootPath.resolve(POM_XML).toFile()))) {
                String line;

                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equals(PACKAGING_JAR)) {
                        type = TYPE_JAR;
                    } else if (line.equals(PACKAGING_WAR)) {
                        type = TYPE_WAR;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (null == type) {
            type = resolveSrcWebAppDir().toFile().exists() ? TYPE_WAR : TYPE_JAR;
        }

        return type;
    }

    @Override
    public Path getRootPath() {
        return rootPath;
    }

    private static final String TARGET = "target";

    private static final String CLASSES = "classes";

    private static final String SRC = "src";

    private static final String MAIN = "main";

    private static final String WEBAPP = "webapp";

    private static final String POM_XML = "pom.xml";

    private static final String PACKAGING_JAR = "<packaging>jar</packaging>";

    private static final String PACKAGING_WAR = "<packaging>war</packaging>";

}
