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
package org.wildfly.swarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.JBossDeploymentStructureAsset;

/**
 * Non-public class for debugging by the Thorntail developers.
 *
 * @author Bob McWhirter
 */
public class DebugUtils {

    private DebugUtils() {
    }

    public static void dumpJBossDeploymentStructure(Archive archive) {
        System.err.println("--- start jboss-deployment-structure.xml");
        JBossDeploymentStructureAsset asset = archive.as(JARArchive.class).getDescriptorAsset();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(asset.openStream()))) {
            reader.lines().forEach(line -> System.err.println(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("--- end jboss-deployment-structure.xml");
    }
}
