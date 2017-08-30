/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.monitor.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.Vetoed;
import javax.naming.NamingException;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.DigestAlgorithm;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.security.impl.CachedAuthenticatedSessionMechanism;
import io.undertow.security.impl.DigestAuthenticationMechanism;
import io.undertow.security.impl.DigestQop;
import io.undertow.security.impl.SimpleNonceManager;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PredicateHandler;
import org.jboss.as.domain.http.server.security.AuthenticationMechanismWrapper;
import org.jboss.as.domain.http.server.security.RealmIdentityManager;
import org.jboss.as.domain.management.AuthMechanism;
import org.jboss.as.domain.management.SecurityRealm;

/**
 * Wraps the actual HTTP endpoint and add security to it.
 *
 * @author Heiko Braun
 * @see HttpContexts
 * @since 18/02/16
 */
@Vetoed
public class SecureHttpContexts implements HttpHandler {


    public SecureHttpContexts(HttpHandler next) {
        this.next = next;

        try {
            this.monitor = Monitor.lookup();
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup monitor", e);
        }

        Optional<SecurityRealm> securityRealm = monitor.getSecurityRealm();

        if (securityRealm.isPresent()) {
            delegate = secureHandler(new HttpContexts(next), securityRealm.get());
        } else {
            delegate = new HttpContexts(next);
        }


    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        delegate.handleRequest(exchange);
    }

    /**
     * Wraps the target handler and makes it inheritSecurity.
     * Includes a predicate for relevant web contexts.
     */
    private HttpHandler secureHandler(final HttpHandler toWrap, SecurityRealm securityRealm) {
        HttpHandler handler = toWrap;

        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);

        RealmIdentityManager idm = new RealmIdentityManager(securityRealm);

        Set<AuthMechanism> mechanisms = securityRealm.getSupportedAuthenticationMechanisms();
        List<AuthenticationMechanism> undertowMechanisms = new ArrayList<AuthenticationMechanism>(mechanisms.size());
        undertowMechanisms.add(wrap(new CachedAuthenticatedSessionMechanism(), null));
        for (AuthMechanism current : mechanisms) {
            switch (current) {
                case DIGEST:
                    List<DigestAlgorithm> digestAlgorithms = Collections.singletonList(DigestAlgorithm.MD5);
                    List<DigestQop> digestQops = Collections.singletonList(DigestQop.AUTH);
                    undertowMechanisms.add(wrap(new DigestAuthenticationMechanism(digestAlgorithms, digestQops,
                                                                                  securityRealm.getName(), "Monitor", new SimpleNonceManager()), current));
                    break;
                case PLAIN:
                    undertowMechanisms.add(wrap(new BasicAuthenticationMechanism(securityRealm.getName()), current));
                    break;
                case LOCAL:
                    break;
                default:
            }
        }

        handler = new AuthenticationMechanismsHandler(handler, undertowMechanisms);
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, idm, handler);

        // the predicate handler takes care that all of the above
        // will only be enacted on relevant web contexts
        handler = new PredicateHandler(exchange -> {
            if (!monitor.getSecurityRealm().isPresent()) {
                return false;
            }

            if (Queries.isAggregatorEndpoint(monitor, exchange.getRelativePath())) {
                return true;
            }

            if (Queries.isDirectAccessToHealthEndpoint(monitor, exchange.getRelativePath())) {
                if (!hasTokenAuth(exchange)) {
                    return true;
                }
                return false;
            }

            if (HttpContexts.getDefaultContextNames().contains(exchange.getRelativePath())) {
                return true;
            }

            return false;

        }, handler, toWrap);

        return handler;
    }

    private boolean hasTokenAuth(HttpServerExchange exchange) {

        String token = exchange.getAttachment(HttpContexts.TOKEN);
        return token != null && HttpContexts.EPHEMERAL_TOKEN.equals(token);
    }

    private static AuthenticationMechanism wrap(final AuthenticationMechanism toWrap, final AuthMechanism mechanism) {
        return new AuthenticationMechanismWrapper(toWrap, mechanism);
    }

    private final HttpHandler delegate;

    private final Monitor monitor;

    private final HttpHandler next;
}

