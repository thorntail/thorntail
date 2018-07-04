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

import org.joox.Match;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.joox.JOOX.$;
import static org.wildfly.swarm.plugin.maven.migrate.MavenUtils.parsePomXml;

final class MigrateWildFlySwarmVersionProperty implements MigrationAction {
    private final Path rootDir;
    private final String versionProperty;
    private final String targetValue;

    MigrateWildFlySwarmVersionProperty(Path rootDir, String versionProperty, String targetValue) {
        this.rootDir = rootDir;
        this.versionProperty = versionProperty;
        this.targetValue = targetValue;
    }

    @Override
    public String describe() {
        return "migrate version property " + versionProperty + " to value " + targetValue;
    }

    @Override
    public void execute() throws Exception {
        // this is not entirely precise, but respecting Maven POM inheritance hierarchy and profile scoping
        // would be just too cumbersome

        List<Path> pomXmls;
        try (Stream<Path> pomXmlsStream = Files.walk(rootDir)) {
            pomXmls = pomXmlsStream
                    .filter(Files::isRegularFile)
                    .filter(p -> "pom.xml".equals(p.getFileName().toString()))
                    .collect(Collectors.toList());
        }

        for (Path pomXml : pomXmls) {
            AtomicBoolean changed = new AtomicBoolean(false);

            Match pom = parsePomXml(pomXml);
            pom.xpath("/m:project/m:properties | /m:project/m:profiles/m:profile/m:properties")
                    .children(property -> versionProperty.equals($(property).tag()))
                    .each(property -> {
                        $(property).text(targetValue);
                        changed.set(true);
                    });

            if (changed.get()) {
                MavenUtils.writePomXml(pomXml, pom);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MigrateWildFlySwarmVersionProperty)) {
            return false;
        }
        MigrateWildFlySwarmVersionProperty that = (MigrateWildFlySwarmVersionProperty) o;
        return Objects.equals(versionProperty, that.versionProperty) &&
                Objects.equals(targetValue, that.targetValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionProperty, targetValue);
    }
}
