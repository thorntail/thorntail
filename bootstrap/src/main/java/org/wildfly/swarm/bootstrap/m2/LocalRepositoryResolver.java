/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.m2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Bob McWhirter
 */
public class LocalRepositoryResolver extends RepositoryResolver {
    @Override
    public File resolve(String gav) throws IOException {

        Path m2repo = findM2Repo();
        Path artifactPath = m2repo.resolve(gavToPath(gav));

        if (Files.notExists(artifactPath)) {
            return null;
        }

        return artifactPath.toFile();
    }

    private Path findM2Repo() {
        Path m2repo = Paths.get(System.getProperty("user.home"), ".m2", "repository");
        if (Files.notExists(m2repo)) {
            String mavenHome = System.getenv("MAVEN_HOME");
            if (mavenHome != null) {
                m2repo = Paths.get(mavenHome, "repository");
                if (Files.notExists(m2repo)) {
                    return null;
                }
            }
        }

        return m2repo;
    }
}
