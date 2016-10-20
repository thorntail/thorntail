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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * @author Ken Finnigan
 */
public final class MavenProfileLoader {

    private static final Pattern profilePattern = Pattern.compile("-P([\\w\\-,]+)");

    private static String[] profiles = new String[0];

    private static boolean profilesDiscovered = false;

    private MavenProfileLoader() {
    }

    public static PomEquippedResolveStage loadPom(ConfigurableMavenResolverSystem resolver) {
        return resolver.loadPomFromFile("pom.xml", determineProfiles());
    }

    public static String[] determineProfiles() {
        if (!profilesDiscovered) {
            String mavenArgs = System.getProperty("env.MAVEN_CMD_LINE_ARGS");

            if (mavenArgs != null) {
                final Matcher matcher = profilePattern.matcher(mavenArgs);
                if (matcher.find()) {
                    String activatedProfiles = matcher.group(1);
                    profiles = activatedProfiles.split(",");
                }
                profilesDiscovered = true;
            }
        }

        return profiles;
    }
}
