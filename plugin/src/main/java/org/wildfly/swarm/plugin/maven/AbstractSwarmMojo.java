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
package org.wildfly.swarm.plugin.maven;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSwarmMojo extends AbstractMojo {

    protected static String VERSION;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected DefaultRepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    protected List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(alias = "mainClass")
    protected String mainClass;

    @Parameter(alias = "properties")
    protected Properties properties;

    @Parameter(alias = "propertiesFile")
    protected String propertiesFile;

    @Parameter(alias = "environment")
    protected Properties environment;

    @Parameter(alias = "environmentFile")
    protected String environmentFile;

    @Parameter(alias = "modules")
    protected String[] additionalModules;

    AbstractSwarmMojo() {
        if (this.additionalModules == null) {
            this.additionalModules = new String[] {"modules"};
        }
    }

    protected void initProperties(final boolean withMaven) throws MojoFailureException {
        if (this.properties == null) {
            this.properties = new Properties();
        }
        if (this.propertiesFile != null) {
            this.properties.putAll(loadProperties(this.propertiesFile));
        }
        // copy any jboss.*, swarm.*, maven.*, or wildfly.* sysprops from System,
        // along with anything that shadows a specified property
        System.getProperties().stringPropertyNames().forEach(key -> {
            if (key.startsWith("jboss.") ||
                    key.startsWith("swarm.") ||
                    key.startsWith("wildfly.") ||
                    (withMaven && key.startsWith("maven.")) ||
                    this.properties.containsKey(key)) {
                this.properties.put(key, System.getProperty(key));
            }
        });

    }

    protected void initEnvironment() throws MojoFailureException {
        if (this.environment == null) {
            this.environment = new Properties();
        }
        if (this.environmentFile != null) {
            this.environment.putAll(loadProperties(this.environmentFile));
        }
    }

    protected static Properties loadProperties(final InputStream in) throws IOException {
        final Properties props = new Properties();
        try {
            props.load(in);
        } finally {
            in.close();
        }

        return props;
    }

    protected static Properties loadProperties(final String file) throws MojoFailureException {
        return loadProperties(new File(file));
    }

    protected static Properties loadProperties(final File file) throws MojoFailureException {
        try {

            return loadProperties(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new MojoFailureException("No such file: " + file, e);
        } catch (IOException e) {
            throw new MojoFailureException("Error reading file: " + file, e);
        }
    }

    static {
        try {
            VERSION = loadProperties(PackageMojo.class
                                             .getClassLoader()
                                             .getResourceAsStream("META-INF/maven/org.wildfly.swarm/wildfly-swarm-plugin/pom.properties"))
                    .getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
