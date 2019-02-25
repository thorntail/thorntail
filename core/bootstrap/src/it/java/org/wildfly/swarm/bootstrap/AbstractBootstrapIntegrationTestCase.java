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
package org.wildfly.swarm.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.bootstrap.env.WildFlySwarmManifest;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.jdk.specific.JarFiles;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractBootstrapIntegrationTestCase {

    protected ClassLoader createClassLoader(JavaArchive archive) throws IOException {
        File tmpFile = export(archive);
        return new URLClassLoader(new URL[]{tmpFile.toURI().toURL()}, null);
    }

    protected ClassLoader createClassLoader(JavaArchive archive, ClassLoader parent) throws IOException {
        File tmpFile = export(archive);
        return new URLClassLoader(new URL[]{tmpFile.toURI().toURL()}, parent);
    }

    protected File export(JavaArchive archive) throws IOException {
        File tmpFile = File.createTempFile("boostrap-archive", ".jar");
        tmpFile.deleteOnExit();
        tmpFile.delete();
        archive.as(ZipExporter.class).exportTo(tmpFile);
        return tmpFile;
    }

    protected JavaArchive createBootstrapArchive() throws IOException {
        return createBootstrapArchive(null);
    }

    protected JavaArchive createBootstrapArchive(String mainClassName) throws IOException {
        return createBootstrapArchive(mainClassName, null);
    }

    protected JavaArchive createBootstrapArchive(String mainClassName, String appArtifact) throws IOException {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.as(ZipImporter.class).importFrom(JarFiles.create(findBootstrapJar()));


        Properties props = new Properties();
        if (appArtifact != null) {
            props.put(BootstrapProperties.APP_ARTIFACT, appArtifact);
        }
        ByteArrayOutputStream propsOut = new ByteArrayOutputStream();
        props.store(propsOut, "");
        propsOut.close();
        archive.addAsManifestResource(new ByteArrayAsset(propsOut.toByteArray()), "wildfly-swarm.properties");

        if (appArtifact != null) {
            WildFlySwarmManifest manifest = new WildFlySwarmManifest();
            manifest.setAsset( appArtifact );
            archive.add(new StringAsset(manifest.toString()), WildFlySwarmManifest.CLASSPATH_LOCATION );
        }

        if (mainClassName != null) {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(new Attributes.Name("Wildfly-Swarm-Main-Class"), mainClassName);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            manifest.write(out);
            out.close();
            archive.addAsManifestResource(new ByteArrayAsset(out.toByteArray()), "MANIFEST.MF");
        }
        return archive;
    }

    protected File findBootstrapJar() {
        Path targetDir = Paths.get("target");

        File[] children = targetDir.toFile().listFiles();
        for (File child : children) {
            if (child.getName().startsWith("bootstrap") && child.getName().endsWith(".jar") && !child.getName().endsWith("-sources.jar")) {
                return child;
            }
        }

        return null;
    }
}
