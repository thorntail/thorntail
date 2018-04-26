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
package org.wildfly.swarm.internal;

import java.io.File;

import org.wildfly.swarm.bootstrap.util.TempFileManager;

/**
 * The only purpose of this util class is to make the exploded app dir (located in tmp) accessible to fractions which do not depend on bootstrap module.
 *
 * @author Martin Kouba
 */
public class ExplodedApplicationArtifactLocator {

    private ExplodedApplicationArtifactLocator() {
    }

    public static File get() {
        return TempFileManager.INSTANCE.getExplodedApplicationArtifact();
    }

}
