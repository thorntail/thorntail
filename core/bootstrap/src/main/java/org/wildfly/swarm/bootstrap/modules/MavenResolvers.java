/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import org.jboss.modules.maven.MavenResolver;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;

/**
 * @author Bob McWhirter
 */
public class MavenResolvers {

    private static BootstrapLogger LOGGER = BootstrapLogger.logger("org.wildfly.swarm.bootstrap");

    public static synchronized ArtifactResolver get() {
        return INSTANCE;
    }

    public static synchronized MavenResolver getForJBossModules() {
        return new JBossModulesMavenResolver(get());
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
                LOGGER.info("Dependencies not bundled; resolving from Gradle cache.");
                INSTANCE.addResolver(new GradleResolver(gradleCachePath));
            } else {
                LOGGER.info("Dependencies not bundled; resolving from M2REPO.");
                INSTANCE.addResolver(ArtifactResolver.wrapJBossModulesResolver(MavenResolver.createDefaultResolver()));
            }
        }
    }

    private MavenResolvers() {
    }
}
