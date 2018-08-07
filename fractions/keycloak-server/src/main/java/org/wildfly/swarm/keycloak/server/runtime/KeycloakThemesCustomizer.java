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

import static org.wildfly.swarm.spi.api.Defaultable.bool;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.config.keycloak.server.Theme;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.keycloak.server.KeycloakServerFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.annotations.Configurable;
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
    public void customize() throws ModuleLoadException, IOException {
        // KC iterates over the well-known folder and class-path theme provides when loading the themes.
        // When only a 'modules' property is set, KC still loads a FolderThemeResolver with a null 'dir' property.
        // Setting a 'dir' property to the current folder is a workaround to avoid FolderThemeResolver failing with
        // NPE for the class-path provider be able to load the themes resources.

        if (!this.keycloakServer.subresources().themes().isEmpty()) {
            this.keycloakServer.subresources().themes().stream().filter((t) -> t.modules() != null && t.dir() == null)
                .forEach((t) -> {
                t.dir(".");
            });
            if (combineDefaultAndCustomThemes.get()) {
                Theme<?> theme = this.keycloakServer.subresources().themes().get(0);
                theme.modules().add(0, "org.keycloak.keycloak-themes");
            }
        } else {

            this.keycloakServer.theme("defaults", (theme) -> {
                theme.module("org.keycloak.keycloak-themes")
                    .staticmaxage(2592000L)
                    .cachethemes(true)
                    .cachetemplates(true)
                    .dir(".");
            });
        }

    }

    /**
     * Whether or not to combine the default themes when the custom themes are also available.
     */
    @Configurable("swarm.keycloak-server.combine-default-and-custom-themes")
    @AttributeDocumentation("Combine the default themes with the custom themes")
    private Defaultable<Boolean> combineDefaultAndCustomThemes = bool(false);
}
