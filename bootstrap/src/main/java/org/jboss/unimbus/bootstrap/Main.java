/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.unimbus.bootstrap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ken Finnigan
 */
public class Main {

    private static final String MODULES_DIR = "modules";

    public static void main(String... args) throws URISyntaxException, IOException {
        // Retrieve module directories
        URL modulesUrl = Main.class.getClassLoader().getResource(MODULES_DIR);

        if (modulesUrl == null) {
            System.out.println("Unable to find modules to load, exiting.");
            return;
        }

        List<Path> modulePaths = Files.list(Paths.get(modulesUrl.toURI())).collect(Collectors.toList());
        modulePaths.forEach(System.out::println);

        // Create Module Layers

        // Start Weld Container
    }
}
