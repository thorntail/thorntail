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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.wildfly.swarm.bootstrap.util.UberJarManifest;

/**
 * @author Bob McWhirter
 */
public class UberJarManifestAsset implements NamedAsset {

    public UberJarManifestAsset(String mainClass, boolean hollow) {
        this.manifest = new UberJarManifest(mainClass, hollow);
    }

    public UberJarManifestAsset(UberJarManifest manifest) {
        this.manifest = manifest;
    }

    @Override
    public String getName() {
        return "META-INF/MANIFEST.MF";
    }

    @Override
    public InputStream openStream() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            this.manifest.write(out);
            out.close();
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            return in;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final UberJarManifest manifest;
}
