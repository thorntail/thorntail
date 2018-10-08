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
package org.wildfly.swarm.swagger.webapp;

import java.io.File;
import java.io.IOException;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.annotations.Configurable;

import static org.wildfly.swarm.spi.api.Defaultable.string;
import static org.wildfly.swarm.swagger.webapp.SwaggerWebAppProperties.DEFAULT_CONTEXT;


/**
 * @author Lance Ball
 */
@Configurable("thorntail.swagger.web-app")
public class SwaggerWebAppFraction implements Fraction<SwaggerWebAppFraction> {

    public SwaggerWebAppFraction() {
    }

    public String getContext() {
        return context.get();
    }

    public void setContext(String context) {
        this.context.set(context);
    }

    /**
     * Allows customization of the swagger-ui web interface.
     * The String provided can be one of either:
     *
     * - Path to a directory on disk
     * - Path to a jar/war/zip file on disk
     * - A GAV string with maven coordinates
     *
     * @param content The location of the web resources (see above)
     * @return this
     */
    public SwaggerWebAppFraction addWebContent(String content) {
        if (content == null) {
            return this;
        }

        if (content.equals("")) {
            return this;
        }

        File maybeFile = new File(content);

        if (!maybeFile.exists()) {
            // the content string is a GAV
            try {
                this.webContent = ArtifactLookup.get().artifact(content);
            } catch (Exception e) {
                SwaggerWebAppMessages.MESSAGES.unableToLocateWebContent(content);
            }
        } else if (maybeFile.isDirectory()) {
            try {
                this.webContent = loadFromDirectory(maybeFile);
            } catch (IOException e) {
                SwaggerWebAppMessages.MESSAGES.unableToLocateWebContent(maybeFile.toString());
            }
        } else {
            this.webContent = ShrinkWrap.createFromZipFile(JARArchive.class, maybeFile);
        }
        return this;
    }

    public Archive<?> getWebContent() {
        return this.webContent;
    }

    private Archive<?> loadFromDirectory(File directory) throws IOException {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.as(ExplodedImporter.class).importDirectory(directory);
        return archive;
    }

    @AttributeDocumentation("Web context path for Swagger end point")
    private Defaultable<String> context = string(DEFAULT_CONTEXT);

    private Archive<?> webContent;
}
