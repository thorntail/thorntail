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

package org.wildfly.swarm.microprofile.openapi.api.models.security;

import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link OAuthFlows} OpenAPI model interface.
 */
public class OAuthFlowsImpl extends ExtensibleImpl implements OAuthFlows, ModelImpl {

    private OAuthFlow implicit;
    private OAuthFlow password;
    private OAuthFlow clientCredentials;
    private OAuthFlow authorizationCode;

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#getImplicit()
     */
    @Override
    public OAuthFlow getImplicit() {
        return this.implicit;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#setImplicit(org.eclipse.microprofile.openapi.models.security.OAuthFlow)
     */
    @Override
    public void setImplicit(OAuthFlow implicit) {
        this.implicit = implicit;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#implicit(org.eclipse.microprofile.openapi.models.security.OAuthFlow)
     */
    @Override
    public OAuthFlows implicit(OAuthFlow implicit) {
        this.implicit = implicit;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#getPassword()
     */
    @Override
    public OAuthFlow getPassword() {
        return this.password;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#setPassword(org.eclipse.microprofile.openapi.models.security.OAuthFlow)
     */
    @Override
    public void setPassword(OAuthFlow password) {
        this.password = password;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#password(org.eclipse.microprofile.openapi.models.security.OAuthFlow)
     */
    @Override
    public OAuthFlows password(OAuthFlow password) {
        this.password = password;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#getClientCredentials()
     */
    @Override
    public OAuthFlow getClientCredentials() {
        return this.clientCredentials;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#setClientCredentials(org.eclipse.microprofile.openapi.models.security.OAuthFlow)
     */
    @Override
    public void setClientCredentials(OAuthFlow clientCredentials) {
        this.clientCredentials = clientCredentials;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#clientCredentials(org.eclipse.microprofile.openapi.models.security.OAuthFlow)
     */
    @Override
    public OAuthFlows clientCredentials(OAuthFlow clientCredentials) {
        this.clientCredentials = clientCredentials;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#getAuthorizationCode()
     */
    @Override
    public OAuthFlow getAuthorizationCode() {
        return this.authorizationCode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#setAuthorizationCode(org.eclipse.microprofile.openapi.models.security.OAuthFlow)
     */
    @Override
    public void setAuthorizationCode(OAuthFlow authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlows#authorizationCode(org.eclipse.microprofile.openapi.models.security.OAuthFlow)
     */
    @Override
    public OAuthFlows authorizationCode(OAuthFlow authorizationCode) {
        this.authorizationCode = authorizationCode;
        return this;
    }

}