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
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import io.thorntail.openapi.impl.api.models.ModelImpl;

/**
 * An implementation of the {@link OAuthFlows} OpenAPI model interface.
 */
public class OAuthFlowsImpl extends ExtensibleImpl implements OAuthFlows, ModelImpl {

    private OAuthFlow implicit;

    private OAuthFlow password;

    private OAuthFlow clientCredentials;

    private OAuthFlow authorizationCode;

    /**
     * @see OAuthFlows#getImplicit()
     */
    @Override
    public OAuthFlow getImplicit() {
        return this.implicit;
    }

    /**
     * @see OAuthFlows#setImplicit(OAuthFlow)
     */
    @Override
    public void setImplicit(OAuthFlow implicit) {
        this.implicit = implicit;
    }

    /**
     * @see OAuthFlows#implicit(OAuthFlow)
     */
    @Override
    public OAuthFlows implicit(OAuthFlow implicit) {
        this.implicit = implicit;
        return this;
    }

    /**
     * @see OAuthFlows#getPassword()
     */
    @Override
    public OAuthFlow getPassword() {
        return this.password;
    }

    /**
     * @see OAuthFlows#setPassword(OAuthFlow)
     */
    @Override
    public void setPassword(OAuthFlow password) {
        this.password = password;
    }

    /**
     * @see OAuthFlows#password(OAuthFlow)
     */
    @Override
    public OAuthFlows password(OAuthFlow password) {
        this.password = password;
        return this;
    }

    /**
     * @see OAuthFlows#getClientCredentials()
     */
    @Override
    public OAuthFlow getClientCredentials() {
        return this.clientCredentials;
    }

    /**
     * @see OAuthFlows#setClientCredentials(OAuthFlow)
     */
    @Override
    public void setClientCredentials(OAuthFlow clientCredentials) {
        this.clientCredentials = clientCredentials;
    }

    /**
     * @see OAuthFlows#clientCredentials(OAuthFlow)
     */
    @Override
    public OAuthFlows clientCredentials(OAuthFlow clientCredentials) {
        this.clientCredentials = clientCredentials;
        return this;
    }

    /**
     * @see OAuthFlows#getAuthorizationCode()
     */
    @Override
    public OAuthFlow getAuthorizationCode() {
        return this.authorizationCode;
    }

    /**
     * @see OAuthFlows#setAuthorizationCode(OAuthFlow)
     */
    @Override
    public void setAuthorizationCode(OAuthFlow authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    /**
     * @see OAuthFlows#authorizationCode(OAuthFlow)
     */
    @Override
    public OAuthFlows authorizationCode(OAuthFlow authorizationCode) {
        this.authorizationCode = authorizationCode;
        return this;
    }

}