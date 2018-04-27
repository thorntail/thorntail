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

package io.thorntail.openapi.impl.api.models.info;

import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import io.thorntail.openapi.impl.api.models.ModelImpl;
import org.eclipse.microprofile.openapi.models.info.Contact;

/**
 * An implementation of the {@link Contact} OpenAPI model interface.
 */
public class ContactImpl extends ExtensibleImpl implements Contact, ModelImpl {

    private String name;

    private String url;

    private String email;

    /**
     * @see Contact#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see Contact#setName(String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see Contact#name(String)
     */
    @Override
    public Contact name(String name) {
        this.name = name;
        return this;
    }

    /**
     * @see Contact#getUrl()
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * @see Contact#setUrl(String)
     */
    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @see Contact#url(String)
     */
    @Override
    public Contact url(String url) {
        this.url = url;
        return this;
    }

    /**
     * @see Contact#getEmail()
     */
    @Override
    public String getEmail() {
        return this.email;
    }

    /**
     * @see Contact#setEmail(String)
     */
    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @see Contact#email(String)
     */
    @Override
    public Contact email(String email) {
        this.email = email;
        return this;
    }

}