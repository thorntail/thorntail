/**
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.jwtauth.deployment.auth;

import java.security.Principal;
import java.security.acl.Group;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.security.auth.Subject;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.undertow.UndertowLogger;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpServerExchange;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi.MPJWTProducer;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.jaas.JWTCredential;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.identity.RoleGroup;
import org.jboss.security.identity.plugins.SimpleRoleGroup;

import static io.undertow.util.Headers.AUTHORIZATION;
import static io.undertow.util.Headers.WWW_AUTHENTICATE;
import static io.undertow.util.StatusCodes.UNAUTHORIZED;

/**
 * An AuthenticationMechanism that validates a caller based on a MicroProfile JWT bearer token
 */
public class JWTAuthMechanism implements AuthenticationMechanism {
    private JWTAuthContextInfo authContextInfo;

    private IdentityManager identityManager;

    public JWTAuthMechanism(JWTAuthContextInfo authContextInfo) {
        this.authContextInfo = authContextInfo;
    }

    /**
     * Extract the Authorization header and validate the bearer token if it exists. If it does, and is validated, this
     * builds the org.jboss.security.SecurityContext authenticated Subject that drives the container APIs as well as
     * the authorization layers.
     *
     * @param exchange        - the http request exchange object
     * @param securityContext - the current security context that
     * @return one of AUTHENTICATED, NOT_AUTHENTICATED or NOT_ATTEMPTED depending on the header and authentication outcome.
     */
    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
        List<String> authHeaders = exchange.getRequestHeaders().get(AUTHORIZATION);
        if (authHeaders != null) {
            String bearerToken = null;
            for (String current : authHeaders) {
                if (current.toLowerCase(Locale.ENGLISH).startsWith("bearer ")) {
                    bearerToken = current.substring(7);
                    if (UndertowLogger.SECURITY_LOGGER.isTraceEnabled()) {
                        UndertowLogger.SECURITY_LOGGER.tracef("Bearer token: %s", bearerToken);
                    }
                    try {
                        identityManager = securityContext.getIdentityManager();
                        JWTCredential credential = new JWTCredential(bearerToken, authContextInfo);
                        if (UndertowLogger.SECURITY_LOGGER.isTraceEnabled()) {
                            UndertowLogger.SECURITY_LOGGER.tracef("Bearer token: %s", bearerToken);
                        }
                        // Install the JWT principal as the caller
                        Account account = identityManager.verify(credential.getName(), credential);
                        if (account != null) {
                            JsonWebToken jwtPrincipal = (JsonWebToken) account.getPrincipal();
                            MPJWTProducer.setJWTPrincipal(jwtPrincipal);
                            JWTAccount jwtAccount = new JWTAccount(jwtPrincipal, account);
                            securityContext.authenticationComplete(jwtAccount, "MP-JWT", false);
                            // Workaround authenticated JsonWebToken not being installed as user principal
                            // https://issues.jboss.org/browse/WFLY-9212
                            org.jboss.security.SecurityContext jbSC = SecurityContextAssociation.getSecurityContext();
                            Subject subject = jbSC.getUtil().getSubject();
                            jbSC.getUtil().createSubjectInfo(jwtPrincipal, bearerToken, subject);
                            RoleGroup roles = extract(subject);
                            jbSC.getUtil().setRoles(roles);
                            UndertowLogger.SECURITY_LOGGER.debugf("Authenticated caller(%s) for path(%s) with roles: %s",
                                    credential.getName(), exchange.getRequestPath(), account.getRoles());
                            return AuthenticationMechanismOutcome.AUTHENTICATED;
                        } else {
                            UndertowLogger.SECURITY_LOGGER.info("Failed to authenticate JWT bearer token");
                            return AuthenticationMechanismOutcome.NOT_AUTHENTICATED;
                        }
                    } catch (Exception e) {
                        UndertowLogger.SECURITY_LOGGER.infof(e, "Failed to validate JWT bearer token");
                        return AuthenticationMechanismOutcome.NOT_AUTHENTICATED;
                    }
                }
            }
        }

        // No suitable header has been found in this request,
        return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
    }

    @Override
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        exchange.getResponseHeaders().add(WWW_AUTHENTICATE, "Bearer {token}");
        UndertowLogger.SECURITY_LOGGER.debugf("Sending Bearer {token} challenge for %s", exchange);
        return new ChallengeResult(true, UNAUTHORIZED);
    }

    /**
     * Extract the Roles group and return it as a RoleGroup
     *
     * @param subject authenticated subject
     * @return RoleGroup from "Roles"
     */
    protected RoleGroup extract(Subject subject) {
        Optional<Principal> match = subject.getPrincipals()
                .stream()
                .filter(g -> g.getName().equals(SecurityConstants.ROLES_IDENTIFIER))
                .findFirst();
        Group rolesGroup = (Group) match.get();
        RoleGroup roles = new SimpleRoleGroup(rolesGroup);
        return roles;
    }
}
