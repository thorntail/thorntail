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
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link Contact} OpenAPI model interface.
 */
public class ContactImpl extends ExtensibleImpl implements Contact, ModelImpl {

    private String name;
    private String url;
    private String email;

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#name(java.lang.String)
     */
    @Override
    public Contact name(String name) {
        this.name = name;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#getUrl()
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#setUrl(java.lang.String)
     */
    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#url(java.lang.String)
     */
    @Override
    public Contact url(String url) {
        this.url = url;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#getEmail()
     */
    @Override
    public String getEmail() {
        return this.email;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#setEmail(java.lang.String)
     */
    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.Contact#email(java.lang.String)
     */
    @Override
    public Contact email(String email) {
        this.email = email;
        return this;
    }

}