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

package io.thorntail.openapi.impl.api.models.security;

import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import io.thorntail.openapi.impl.api.models.ModelImpl;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import io.thorntail.openapi.impl.OpenApiConstants;

/**
 * An implementation of the {@link SecurityScheme} OpenAPI model interface.
 */
public class SecuritySchemeImpl extends ExtensibleImpl implements SecurityScheme, ModelImpl {

    private String $ref;

    private Type type;

    private String description;

    private String name;

    private In in;

    private String scheme;

    private String bearerFormat;

    private OAuthFlows flows;

    private String openIdConnectUrl;

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#getRef()
     */
    @Override
    public String getRef() {
        return this.$ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_SECURITY_SCHEME + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(String)
     */
    @Override
    public SecurityScheme ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see SecurityScheme#getType()
     */
    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * @see SecurityScheme#setType(Type)
     */
    @Override
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @see SecurityScheme#type(Type)
     */
    @Override
    public SecurityScheme type(Type type) {
        this.type = type;
        return this;
    }

    /**
     * @see SecurityScheme#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see SecurityScheme#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see SecurityScheme#description(String)
     */
    @Override
    public SecurityScheme description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see SecurityScheme#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see SecurityScheme#setName(String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see SecurityScheme#name(String)
     */
    @Override
    public SecurityScheme name(String name) {
        this.name = name;
        return this;
    }

    /**
     * @see SecurityScheme#getIn()
     */
    @Override
    public In getIn() {
        return this.in;
    }

    /**
     * @see SecurityScheme#setIn(In)
     */
    @Override
    public void setIn(In in) {
        this.in = in;
    }

    /**
     * @see SecurityScheme#in(In)
     */
    @Override
    public SecurityScheme in(In in) {
        this.in = in;
        return this;
    }

    /**
     * @see SecurityScheme#getScheme()
     */
    @Override
    public String getScheme() {
        return this.scheme;
    }

    /**
     * @see SecurityScheme#setScheme(String)
     */
    @Override
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * @see SecurityScheme#scheme(String)
     */
    @Override
    public SecurityScheme scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * @see SecurityScheme#getBearerFormat()
     */
    @Override
    public String getBearerFormat() {
        return this.bearerFormat;
    }

    /**
     * @see SecurityScheme#setBearerFormat(String)
     */
    @Override
    public void setBearerFormat(String bearerFormat) {
        this.bearerFormat = bearerFormat;
    }

    /**
     * @see SecurityScheme#bearerFormat(String)
     */
    @Override
    public SecurityScheme bearerFormat(String bearerFormat) {
        this.bearerFormat = bearerFormat;
        return this;
    }

    /**
     * @see SecurityScheme#getFlows()
     */
    @Override
    public OAuthFlows getFlows() {
        return this.flows;
    }

    /**
     * @see SecurityScheme#setFlows(OAuthFlows)
     */
    @Override
    public void setFlows(OAuthFlows flows) {
        this.flows = flows;
    }

    /**
     * @see SecurityScheme#flows(OAuthFlows)
     */
    @Override
    public SecurityScheme flows(OAuthFlows flows) {
        this.flows = flows;
        return this;
    }

    /**
     * @see SecurityScheme#getOpenIdConnectUrl()
     */
    @Override
    public String getOpenIdConnectUrl() {
        return this.openIdConnectUrl;
    }

    /**
     * @see SecurityScheme#setOpenIdConnectUrl(String)
     */
    @Override
    public void setOpenIdConnectUrl(String openIdConnectUrl) {
        this.openIdConnectUrl = openIdConnectUrl;
    }

    /**
     * @see SecurityScheme#openIdConnectUrl(String)
     */
    @Override
    public SecurityScheme openIdConnectUrl(String openIdConnectUrl) {
        this.openIdConnectUrl = openIdConnectUrl;
        return this;
    }

}