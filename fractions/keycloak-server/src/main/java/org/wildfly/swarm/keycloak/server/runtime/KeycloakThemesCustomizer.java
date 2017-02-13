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
package org.wildfly.swarm.keycloak.server.runtime;

import java.io.File;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.keycloak.server.KeycloakServerFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class KeycloakThemesCustomizer implements Customizer {

    @Inject
    KeycloakServerFraction keycloakServer;

    @Override
    public void customize() {

        if (!this.keycloakServer.subresources().themes().isEmpty()) {
            return;
        }


        try {
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.keycloak.keycloak-themes"));
            URL resource = module.getExportedResource("keycloak-themes.jar");

            System.err.println("themes.jar: " + resource);
            JARArchive themesArtifact = ShrinkWrap.create(JARArchive.class);
            themesArtifact.as(ZipImporter.class).importFrom(resource.openStream());

            File root = TempFileManager.INSTANCE.newTempDirectory("keycloak-themes", ".d");
            File exportedDir = themesArtifact.as(ExplodedExporter.class).exportExplodedInto(root);
            File themeDir = new File(exportedDir, "theme");

            System.err.println("exported to " + exportedDir.getAbsolutePath());

            this.keycloakServer.theme("defaults", (theme) -> {
                theme.dir(themeDir.getAbsolutePath());
                theme.staticmaxage(2592000L);
                theme.cachethemes(true);
                theme.cachetemplates(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
