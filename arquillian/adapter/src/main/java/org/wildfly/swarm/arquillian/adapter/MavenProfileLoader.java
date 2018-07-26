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
package org.wildfly.swarm.arquillian.adapter;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.wildfly.swarm.internal.MavenArgsParser;
import org.wildfly.swarm.internal.MavenBuildFileResolver;

import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Ken Finnigan
 */
public final class MavenProfileLoader {

    public static final String ENV_MAVEN_CMD_LINE_ARGS = "env.MAVEN_CMD_LINE_ARGS";

    private static String[] profiles = new String[0];

    private static boolean profilesDiscovered = false;

    private MavenProfileLoader() {
    }

    public static PomEquippedResolveStage loadPom(ConfigurableMavenResolverSystem resolver) {
        final String projectRoot = Paths.get("").toAbsolutePath().toString();
        return resolver.loadPomFromFile(MavenBuildFileResolver.resolveMavenBuildFileName(projectRoot).toFile(), determineProfiles());
    }

    public static String[] determineProfiles() {
        if (!profilesDiscovered) {
            String commandLine = System.getProperty(ENV_MAVEN_CMD_LINE_ARGS);

            if (commandLine != null) {
                MavenArgsParser args = MavenArgsParser.parse(commandLine);
                Optional<String> p_arg = args.get(MavenArgsParser.ARG.P);
                if (p_arg.isPresent()) {
                    profiles = p_arg.get().split(",");
                }
                profilesDiscovered = true;
            }
        }

        return profiles;
    }
}
