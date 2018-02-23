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

package org.wildfly.swarm.microprofile.openapi.api.models.info;

import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link Info} OpenAPI model interface.
 */
public class InfoImpl extends ExtensibleImpl implements Info, ModelImpl {

    private String title;
    private String description;
    private String termsOfService;
    private Contact contact;
    private License license;
    private String version;

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#getTitle()
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#title(java.lang.String)
     */
    @Override
    public Info title(String title) {
        this.title = title;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#description(java.lang.String)
     */
    @Override
    public Info description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#getTermsOfService()
     */
    @Override
    public String getTermsOfService() {
        return this.termsOfService;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#setTermsOfService(java.lang.String)
     */
    @Override
    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#termsOfService(java.lang.String)
     */
    @Override
    public Info termsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#getContact()
     */
    @Override
    public Contact getContact() {
        return this.contact;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#setContact(org.eclipse.microprofile.openapi.models.info.Contact)
     */
    @Override
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#contact(org.eclipse.microprofile.openapi.models.info.Contact)
     */
    @Override
    public Info contact(Contact contact) {
        this.contact = contact;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#getLicense()
     */
    @Override
    public License getLicense() {
        return this.license;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#setLicense(org.eclipse.microprofile.openapi.models.info.License)
     */
    @Override
    public void setLicense(License license) {
        this.license = license;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#license(org.eclipse.microprofile.openapi.models.info.License)
     */
    @Override
    public Info license(License license) {
        this.license = license;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#getVersion()
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#setVersion(java.lang.String)
     */
    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Info#version(java.lang.String)
     */
    @Override
    public Info version(String version) {
        this.version = version;
        return this;
    }

}