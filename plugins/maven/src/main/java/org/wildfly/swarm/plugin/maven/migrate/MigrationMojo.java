/**
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.plugin.maven.migrate;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.joox.Match;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.joox.JOOX.$;
import static org.wildfly.swarm.plugin.maven.migrate.MavenUtils.$$;
import static org.wildfly.swarm.plugin.maven.migrate.MavenUtils.parsePomXml;
import static org.wildfly.swarm.plugin.maven.migrate.MavenUtils.propertyNamesReferredFrom;
import static org.wildfly.swarm.plugin.maven.migrate.MavenUtils.refersToProperty;

@Mojo(name = "migrate-from-wildfly-swarm", aggregator = true, requiresDirectInvocation = true)
public class MigrationMojo extends AbstractMojo {
    @Parameter(property = "targetVersion")
    private String targetVersion;

    @Parameter(property = "dryRun", defaultValue = "false")
    private boolean dryRun;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;

    private Path rootDir;

    @Override
    public void execute() throws MojoExecutionException {
        findTargetVersion();

        rootDir = Paths.get("."); // could also use MavenSession.getExecutionRootDirectory()

        try {
            Set<Path> pomXmls = findPomXmls(rootDir);

            Set<MigrationAction> migrationActions = new HashSet<>();
            for (Path pomXml : pomXmls) {
                migrationActions.addAll(prepareMigrationActions(pomXml));
            }

            // sorting just for log readability, doesn't affect outcome
            List<MigrationAction> sortedMigrationActions = migrationActions.stream()
                    .sorted(Comparator.comparing(MigrationAction::describe))
                    .collect(Collectors.toList());
            for (MigrationAction migrationAction : sortedMigrationActions) {
                getLog().info(migrationAction.describe());

                if (!dryRun) {
                    migrationAction.execute();
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void findTargetVersion() {
        if (targetVersion == null || targetVersion.isEmpty()) {
            targetVersion = mojo.getPlugin().getVersion();
        }

        getLog().info("Upgrading to Thorntail " + targetVersion);

    }

    private Set<Path> findPomXmls(Path rootDir) throws IOException {
        try (Stream<Path> pomXmls = Files.walk(rootDir)) {
            return pomXmls
                    .filter(Files::isRegularFile)
                    .filter(p -> "pom.xml".equals(p.getFileName().toString()))
                    .collect(Collectors.toSet());
        }
    }

    private Set<MigrationAction> prepareMigrationActions(Path pomXml) throws IOException, SAXException, ParserConfigurationException {
        Match pom = parsePomXml(pomXml);

        Set<MigrationAction> result = new HashSet<>();

        Match nonProfile = pom.xpath("/m:project");
        result.addAll(prepareMigrationActionsForProfile(pomXml, nonProfile, ProfileLocation.none()));

        pom.xpath("/m:project/m:profiles/m:profile").each(profile -> {
            result.addAll(prepareMigrationActionsForProfile(pomXml, $$(profile), ProfileLocation.profile($(profile))));
        });

        return result;
    }

    private Set<MigrationAction> prepareMigrationActionsForProfile(Path pomXml, Match profile, ProfileLocation profileLocation) {
        Set<MigrationAction> result = new HashSet<>();

        Match dependencies = profile.xpath("m:dependencies/m:dependency");
        result.addAll(prepareMigrationActionsForDependencies(pomXml, dependencies,
                dependency -> DependencyLocation.dependencies(PluginLocation.none(profileLocation), dependency)));

        Match dependencyManagement = profile.xpath("m:dependencyManagement/m:dependencies/m:dependency");
        result.addAll(prepareMigrationActionsForDependencies(pomXml, dependencyManagement,
                dependency -> DependencyLocation.dependencyManagement(PluginLocation.none(profileLocation), dependency)));

        Match plugins = profile.xpath("m:build/m:plugins/m:plugin");
        result.addAll(prepareMigrationActionsForPlugins(pomXml, plugins,
                plugin -> PluginLocation.plugins(profileLocation, plugin)));

        Match pluginManagement = profile.xpath("m:build/m:pluginManagement/m:plugins/m:plugin");
        result.addAll(prepareMigrationActionsForPlugins(pomXml, pluginManagement,
                plugin -> PluginLocation.pluginManagement(profileLocation, plugin)));

        return result;
    }

    private Set<MigrationAction> prepareMigrationActionsForDependencies(Path pomXml, Match dependencies, Function<Match, DependencyLocation> locator) {
        Set<MigrationAction> result = new HashSet<>();

        dependencies.each(dependency -> {
            String groupId = $(dependency).child("groupId").text();
            String version = $(dependency).child("version").text();

            if (isWildFlySwarmGroupId(groupId)) {
                result.add(new MigrateWildFlySwarmDependency(pomXml, locator.apply($(dependency)), targetThorntailVersion(version)));

                if (refersToProperty(version)) {
                    for (String property : propertyNamesReferredFrom(version)) {
                        result.add(new MigrateWildFlySwarmVersionProperty(rootDir, property, targetVersion));
                    }
                }
            }

            $(dependency).find("exclusion").each(exclusion -> {
                String exclusionGroupId = $(exclusion).child("groupId").text();
                String exclusionArtifactId = $(exclusion).child("artifactId").text();
                if (isWildFlySwarmGroupId(exclusionGroupId)) {
                    result.add(new MigrateWildFlySwarmDependencyExclusion(pomXml, locator.apply($(dependency)), exclusionGroupId, exclusionArtifactId));
                }
            });
        });

        return result;
    }

    private Set<MigrationAction> prepareMigrationActionsForPlugins(Path pomXml, Match plugins, Function<Match, PluginLocation> locator) {
        Set<MigrationAction> result = new HashSet<>();

        plugins.each(plugin -> {
            String groupId = $(plugin).child("groupId").text();
            String artifactId = $(plugin).child("artifactId").text();
            String version = $(plugin).child("version").text();

            if (isWildFlySwarmGroupId(groupId) && "wildfly-swarm-plugin".equals(artifactId)) {
                result.add(new MigrateWildFlySwarmPlugin(pomXml, locator.apply($(plugin)), targetThorntailVersion(version)));
                if (refersToProperty(version)) {
                    for (String property : propertyNamesReferredFrom(version)) {
                        result.add(new MigrateWildFlySwarmVersionProperty(rootDir, property, targetVersion));
                    }
                }
            }

            // TODO Fabric8 Maven plugin?

            Function<Match, DependencyLocation> dependencyLocator =
                    dependency -> DependencyLocation.dependencies(locator.apply($(plugin)), dependency);
            result.addAll(prepareMigrationActionsForDependencies(pomXml, $(plugin).find("dependency"), dependencyLocator));
        });
        return result;
    }

    private String targetThorntailVersion(String currentWildFlySwarmVersion) {
        if (currentWildFlySwarmVersion == null || refersToProperty(currentWildFlySwarmVersion)) {
            return null; // no change
        }
        return targetVersion;
    }

    private static boolean isWildFlySwarmGroupId(String groupId) {
        return "org.wildfly.swarm".equals(groupId);
    }
}
