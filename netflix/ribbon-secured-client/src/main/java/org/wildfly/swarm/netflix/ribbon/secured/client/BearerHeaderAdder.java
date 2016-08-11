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
package org.wildfly.swarm.netflix.ribbon.secured.client;

import com.netflix.loadbalancer.reactive.ExecutionContext;
import com.netflix.loadbalancer.reactive.ExecutionInfo;
import com.netflix.loadbalancer.reactive.ExecutionListener;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.keycloak.KeycloakSecurityContext;
import org.wildfly.swarm.keycloak.deployment.KeycloakSecurityContextAssociation;

/**
 * @author Bob McWhirter
 */
public class BearerHeaderAdder implements ExecutionListener<HttpClientRequest<ByteBuf>, HttpClientResponse<ByteBuf>> {
    @Override
    public void onExecutionStart(ExecutionContext<HttpClientRequest<ByteBuf>> context) throws AbortExecutionException {
    }

    @Override
    public void onStartWithServer(ExecutionContext<HttpClientRequest<ByteBuf>> context, ExecutionInfo info) throws AbortExecutionException {
        KeycloakSecurityContext securityContext = KeycloakSecurityContextAssociation.get();
        if (securityContext != null) {
            HttpClientRequest<ByteBuf> request = context.getRequest();
            request.withHeader("Authorization", "Bearer " + securityContext.getTokenString());
            context.put(KeycloakSecurityContextAssociation.class.getName(), securityContext);
        } else {
            KeycloakSecurityContextAssociation.disassociate();
        }
    }

    @Override
    public void onExceptionWithServer(ExecutionContext<HttpClientRequest<ByteBuf>> context, Throwable exception, ExecutionInfo info) {
        KeycloakSecurityContext securityContext = (KeycloakSecurityContext) context.get(KeycloakSecurityContextAssociation.class.getName());
        if (securityContext != null) {
            KeycloakSecurityContextAssociation.associate(securityContext);
        } else {
            KeycloakSecurityContextAssociation.disassociate();
        }
    }

    @Override
    public void onExecutionSuccess(ExecutionContext<HttpClientRequest<ByteBuf>> context, HttpClientResponse<ByteBuf> response, ExecutionInfo info) {
        KeycloakSecurityContext securityContext = (KeycloakSecurityContext) context.get(KeycloakSecurityContextAssociation.class.getName());
        if (securityContext != null) {
            KeycloakSecurityContextAssociation.associate(securityContext);
        } else {
            KeycloakSecurityContextAssociation.disassociate();
        }
    }

    @Override
    public void onExecutionFailed(ExecutionContext<HttpClientRequest<ByteBuf>> context, Throwable finalException, ExecutionInfo info) {
        KeycloakSecurityContext securityContext = (KeycloakSecurityContext) context.get(KeycloakSecurityContextAssociation.class.getName());
        if (securityContext != null) {
            KeycloakSecurityContextAssociation.associate(securityContext);
        } else {
            KeycloakSecurityContextAssociation.disassociate();
        }
    }
}
