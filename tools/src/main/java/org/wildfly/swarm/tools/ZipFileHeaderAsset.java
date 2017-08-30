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
package org.wildfly.swarm.tools;

import java.io.InputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ZipFileHeaderAsset implements Asset {

    private final ZipFile zipFile;

    private final FileHeader fileHeader;

    public ZipFileHeaderAsset(ZipFile zipFile, FileHeader fileHeader) {
        this.zipFile = zipFile;
        this.fileHeader = fileHeader;
    }

    @Override
    public InputStream openStream() {
        try {
            return zipFile.getInputStream(fileHeader);
        } catch (ZipException e) {
            throw new RuntimeException("Could not open zip file stream", e);
        }
    }
}
