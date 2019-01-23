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
package org.wildfly.swarm.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.jboss.shrinkwrap.api.Node;
import org.wildfly.swarm.fractions.FractionDescriptor;
import org.wildfly.swarm.jdk.specific.JarFiles;

/**
 * Dependencies that have been resolved to local files.
 *
 * @author Heiko Braun
 * @author Ken Finnigan
 * @since 26/10/2016
 */
public interface ResolvedDependencies {

    String WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID = "bootstrap";

    String JBOSS_MODULES_GROUP_ID = "org.jboss.modules";

    String JBOSS_MODULES_ARTIFACT_ID = "jboss-modules";


    Set<ArtifactSpec> getDependencies();

    ArtifactSpec findWildFlySwarmBootstrapJar();

    ArtifactSpec findJBossModulesJar();

    ArtifactSpec findArtifact(String groupId, String artifactId, String version, String packaging, String classifier);

    ArtifactSpec findArtifact(String groupId, String artifactId, String version, String packaging, String classifier, boolean includeTestScope);

    static boolean isExplodedBootstrap(ArtifactSpec dependency) {
        if (dependency.groupId().equals(JBOSS_MODULES_GROUP_ID) && dependency.artifactId().equals(JBOSS_MODULES_ARTIFACT_ID)) {
            return true;
        }
        if (dependency.groupId().equals(FractionDescriptor.THORNTAIL_GROUP_ID) && dependency.artifactId().equals(WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID)) {
            return true;
        }
        return false;
    }

    static Stream<ModuleAnalyzer> findModuleXmls(File file) {
        List<ModuleAnalyzer> analyzers = new ArrayList<>();
        try (JarFile jar = JarFiles.create(file)) {

            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry each = entries.nextElement();
                String name = each.getName();

                if (name.startsWith("modules/") && name.endsWith("module.xml")) {
                    try (InputStream in = jar.getInputStream(each)) {
                        analyzers.add(new ModuleAnalyzer(in));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return analyzers.stream();
    }

    boolean isRemovable(Node node);

    Set<ArtifactSpec> getModuleDependencies();
}
