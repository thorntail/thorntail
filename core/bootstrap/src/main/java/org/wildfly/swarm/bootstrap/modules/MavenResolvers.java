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
package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.maven.MavenResolver;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Bob McWhirter
 */
public class MavenResolvers {

    public static synchronized MavenResolver get() {
        return INSTANCE;
    }

    private static final MultiMavenResolver INSTANCE = new MultiMavenResolver();

    static {
        INSTANCE.addResolver(new UberJarMavenResolver());
        if (System.getProperty(BootstrapProperties.BUNDLED_DEPENDENCIES) == null) {
            // If class path contains ".gradle", we have a gradle build environment.
            Optional<String> gradleClassPathFile = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                    .filter(name -> name.contains(".gradle"))
                    .findFirst();
            if (gradleClassPathFile.isPresent()) {
                String gradleCachePath = gradleClassPathFile.get().substring(0, gradleClassPathFile.get().indexOf("files-2.1") + 9);
                System.err.println("Dependencies not bundled, will resolve from gradle cache: " + gradleCachePath);
                INSTANCE.addResolver(new GradleResolver(gradleCachePath));
            } else {
                System.err.println("Dependencies not bundled, will resolve from local M2REPO");
                INSTANCE.addResolver(MavenResolver.createDefaultResolver());
            }
        }
    }

    private MavenResolvers() {
    }
}
