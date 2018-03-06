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
import org.eclipse.microprofile.openapi.models.security.Scopes;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link OAuthFlow} OpenAPI model interface.
 */
public class OAuthFlowImpl extends ExtensibleImpl implements OAuthFlow, ModelImpl {

    private String authorizationUrl;
    private String tokenUrl;
    private String refreshUrl;
    private Scopes scopes;

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#getAuthorizationUrl()
     */
    @Override
    public String getAuthorizationUrl() {
        return this.authorizationUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#setAuthorizationUrl(java.lang.String)
     */
    @Override
    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#authorizationUrl(java.lang.String)
     */
    @Override
    public OAuthFlow authorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#getTokenUrl()
     */
    @Override
    public String getTokenUrl() {
        return this.tokenUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#setTokenUrl(java.lang.String)
     */
    @Override
    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#tokenUrl(java.lang.String)
     */
    @Override
    public OAuthFlow tokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#getRefreshUrl()
     */
    @Override
    public String getRefreshUrl() {
        return this.refreshUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#setRefreshUrl(java.lang.String)
     */
    @Override
    public void setRefreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#refreshUrl(java.lang.String)
     */
    @Override
    public OAuthFlow refreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#getScopes()
     */
    @Override
    public Scopes getScopes() {
        return this.scopes;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#setScopes(org.eclipse.microprofile.openapi.models.security.Scopes)
     */
    @Override
    public void setScopes(Scopes scopes) {
        this.scopes = scopes;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.security.OAuthFlow#scopes(org.eclipse.microprofile.openapi.models.security.Scopes)
     */
    @Override
    public OAuthFlow scopes(Scopes scopes) {
        this.scopes = scopes;
        return this;
    }

}