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
package org.wildfly.swarm.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Bob McWhirter
 */
public class ArtifactAsset implements ProjectAsset {

    public ArtifactAsset(ArtifactSpec spec, String artifactName) {
        this.spec = spec;
        this.artifactName = artifactName;
    }

    @Override
    public String getSimpleName() {
        if (this.artifactName != null) {

            return this.artifactName;
        }
        final String version = this.spec.version();

        return String.format("%s%s.%s", this.spec.artifactId(),
                             (version == null || version.isEmpty() ? "" : "-" + version),
                             this.spec.type());
    }

    @Override
    public InputStream openStream() {
        try {
            return new FileInputStream(spec.file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Archive<?> getArchive() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, spec.file);
    }

    private final ArtifactSpec spec;

    private final String artifactName;
}
