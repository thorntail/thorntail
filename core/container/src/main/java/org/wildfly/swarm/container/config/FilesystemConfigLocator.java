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
package org.wildfly.swarm.container.config;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class FilesystemConfigLocator extends ConfigLocator {

    public FilesystemConfigLocator() {
        this(Paths.get("."));
    }

    public FilesystemConfigLocator(Path root) {
        this.root = root;
    }

    @Override
    public Stream<URL> locate(String profileName) throws IOException {
        List<URL> located = new ArrayList<>();

        Path path = this.root.resolve(PROJECT_PREFIX + profileName + ".yml");

        if (Files.exists(path)) {
            located.add(path.toUri().toURL());
        }

        path = this.root.resolve(PROJECT_PREFIX + profileName + ".yaml");

        if (Files.exists(path)) {
            located.add(path.toUri().toURL());
        }

        path = this.root.resolve(PROJECT_PREFIX + profileName + ".properties");
        if (Files.exists(path)) {
            located.add(path.toUri().toURL());
        }

        return located.stream();
    }

    private final Path root;

}
