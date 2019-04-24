/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.jwtauth.deployment.auth;

import javax.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi.PrincipalProducer;

import io.undertow.security.idm.Account;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 8/13/18
 */
public class MpJwtPrincipalHandler implements HttpHandler {

    private final HttpHandler next;

    public MpJwtPrincipalHandler(HttpHandler next) {
        this.next = next;
    }

    /**
     * If there is a JWTAccount installed in the exchange security context, create
     *
     * @param exchange - the request/response exchange
     * @throws Exception on failure
     */
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Account account = exchange.getSecurityContext().getAuthenticatedAccount();
        if (account != null && account.getPrincipal() instanceof JsonWebToken) {
            JsonWebToken token = (JsonWebToken)account.getPrincipal();
            JWTAccount jwtAccount = new JWTAccount(token, account);
            PrincipalProducer myInstance = CDI.current().select(PrincipalProducer.class).get();
            myInstance.setAccount(jwtAccount);
        }
        next.handleRequest(exchange);
    }

}
