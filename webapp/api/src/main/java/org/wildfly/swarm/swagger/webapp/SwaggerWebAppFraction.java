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
import java.io.InputStream;
import java.util.Properties;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.undertow.UndertowProperties;


/**
 * @author Lance Ball
 */
public class SwaggerWebAppFraction implements Fraction {

    public static final String VERSION;

    public SwaggerWebAppFraction() {
        context = System.getProperty(UndertowProperties.CONTEXT_PATH, DEFAULT_CONTEXT);
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
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
        if (content == null) return this;
        if (content.equals("")) return this;
        File maybeFile = new File(content);
        if (!maybeFile.exists()) {
            // the content string is a GAV
            try {
                this.webContent = Swarm.artifact(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (maybeFile.isDirectory()) {
            try {
                this.webContent = loadFromDirectory(maybeFile);
            } catch (IOException e) {
                e.printStackTrace();
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

    static {
        InputStream in = SwaggerWebAppFraction.class.getClassLoader().getResourceAsStream("swagger-webapp.properties");
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        VERSION = props.getProperty("version", "unknown");
    }

    private final String DEFAULT_CONTEXT = "/swagger-ui";

    private String context = DEFAULT_CONTEXT;

    private Archive<?> webContent;
}
