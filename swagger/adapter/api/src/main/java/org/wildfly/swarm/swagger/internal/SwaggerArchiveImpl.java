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
package org.wildfly.swarm.swagger.internal;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.msc.ServiceActivatorArchive;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.swagger.SwaggerArchive;

/**
 * @author Lance Ball
 */
public class SwaggerArchiveImpl extends AssignableBase<ArchiveBase<?>> implements SwaggerArchive {

    public static final String SERVICE_ACTIVATOR_CLASS_NAME = "org.wildfly.swarm.swagger.runtime.SwaggerActivator";

    public SwaggerArchiveImpl(ArchiveBase<?> archive) {
        super(archive);

        if (!as(ServiceActivatorArchive.class).containsServiceActivator(SERVICE_ACTIVATOR_CLASS_NAME)) {
            as(ServiceActivatorArchive.class).addServiceActivator(SERVICE_ACTIVATOR_CLASS_NAME);
            as(JARArchive.class).addModule("org.wildfly.swarm.swagger", "runtime");
        }

        loadOrCreateConfigurationAsset();
    }

    public SwaggerConfigurationAsset getConfigurationAsset() {
        return configurationAsset;
    }

    @Override
    public SwaggerArchive setResourcePackages(String... packages) {
        getConfigurationAsset().register(packages);
        return this;
    }

    @Override
    public SwaggerArchive setTitle(String title) {
        getConfigurationAsset().setTitle(title);
        return this;
    }

    @Override
    public SwaggerArchive setDescription(String description) {
        getConfigurationAsset().setDescription(description);
        return this;
    }

    @Override
    public SwaggerArchive setTermsOfServiceUrl(String url) {
        getConfigurationAsset().setTermsOfServiceUrl(url);
        return this;
    }

    @Override
    public SwaggerArchive setContact(String contact) {
        getConfigurationAsset().setContact(contact);
        return this;
    }

    @Override
    public SwaggerArchive setLicense(String license) {
        getConfigurationAsset().setLicense(license);
        return this;
    }

    @Override
    public SwaggerArchive setLicenseUrl(String licenseUrl) {
        getConfigurationAsset().setLicenseUrl(licenseUrl);
        return this;
    }

    @Override
    public SwaggerArchive setVersion(String version) {
        getConfigurationAsset().setVersion(version);
        return this;
    }

    @Override
    public SwaggerArchive setSchemes(String... schemes) {
        getConfigurationAsset().setSchemes(schemes);
        return this;
    }

    @Override
    public SwaggerArchive setHost(String host) {
        getConfigurationAsset().setHost(host);
        return this;
    }

    @Override
    public SwaggerArchive setContextRoot(String root) {
        getConfigurationAsset().setContextRoot(root);
        return this;
    }

    @Override
    public SwaggerArchive setPrettyPrint(boolean prettyPrint) {
        getConfigurationAsset().setPrettyPrint(prettyPrint);
        return this;
    }

    @Override
    public boolean hasResourcePackages() {
        return getConfigurationAsset().getResourcePackages() != null;
    }

    @Override
    public String[] getResourcePackages() {
        return getConfigurationAsset().getResourcePackages();
    }

    private void loadOrCreateConfigurationAsset() {
        Node node = getArchive().get(SWAGGER_CONFIGURATION_PATH);

        if (node != null) {
            Asset asset = node.getAsset();
            if (asset instanceof SwaggerConfigurationAsset) {
                this.configurationAsset = (SwaggerConfigurationAsset) asset;
            } else {
                this.configurationAsset = new SwaggerConfigurationAsset(asset.openStream());
            }
        } else {
            this.configurationAsset = new SwaggerConfigurationAsset();
            getArchive().add(this.configurationAsset, SWAGGER_CONFIGURATION_PATH);
        }
    }

    private SwaggerConfigurationAsset configurationAsset;
}
