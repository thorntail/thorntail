/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.spi.meta;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 *
 * @author Juan Gonzalez
 *
 */
public abstract class PathSource {

    private Path basePath;
    private Path source;

    public PathSource(Path basePath, Path source) {
        super();
        this.basePath = basePath;
        this.source = source;
    }

    public Path getBasePath() {
       return basePath;
    }

    public void setBasePath(Path path) {
        this.basePath = path;
    }

    public Path getSource() {
       return source;
    }

    public void setSource(Path source) {
       this.source = source;
    }

    /**
     * Gets the relative file path, instead of the absolute.
     * @return Relative path from this file, if basePath was provided.
     */
    public String getRelativePath() {
       if (basePath == null) {
           return source.toString();
       }

       return basePath.relativize(source).toString();
    }

    public abstract InputStream getInputStream() throws IOException;

}