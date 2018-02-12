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

import org.eclipse.microprofile.openapi.models.info.License;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link License} OpenAPI model interface.
 */
public class LicenseImpl extends ExtensibleImpl implements License, ModelImpl {

    private String name;
    private String url;

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#name(java.lang.String)
     */
    @Override
    public License name(String name) {
        this.name = name;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#getUrl()
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#setUrl(java.lang.String)
     */
    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.info.License#url(java.lang.String)
     */
    @Override
    public License url(String url) {
        this.url = url;
        return this;
    }

}