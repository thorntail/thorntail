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
package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.jaas;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.smallrye.jwt.auth.principal.ParseException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.SecurityAssociationCallback;
import org.jboss.security.auth.spi.RoleMappingLoginModule;

/**
 *
 */
public class JWTLoginModule extends RoleMappingLoginModule {
    private static final String LOG_EXCEPTIONS = "logExceptions";
    private static final String[] ALL_VALID_OPTIONS = {
            LOG_EXCEPTIONS
    };

    private JsonWebToken jwtPrincipal;
    private boolean logExceptions = false;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        addValidOptions(ALL_VALID_OPTIONS);
        super.initialize(subject, callbackHandler, sharedState, options);
        if (options.containsKey("logExceptions")) {
            logExceptions = Boolean.valueOf(options.get("logExceptions").toString());
        }
    }

    @Override
    public boolean login() throws LoginException {
        SecurityAssociationCallback sac = new SecurityAssociationCallback();
        try {
            callbackHandler.handle(new Callback[]{sac});
            JWTCredential jwtCredential = (JWTCredential) sac.getCredential();
            // Validate the credential by
            jwtPrincipal = validate(jwtCredential);
        } catch (Exception e) {
            if (logExceptions) {
                log.infof(e, "Failed to validate token");
            }
            LoginException ex = new LoginException("Failed to validate token");
            ex.initCause(e);
            throw ex;
        }

        loginOk = true;
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        subject.getPrincipals().add(jwtPrincipal);
        SimpleGroup roles = new SimpleGroup("Roles");
        for (String name : jwtPrincipal.getGroups()) {
            roles.addMember(new SimplePrincipal(name));
        }
        subject.getPrincipals().add(roles);
        sharedState.put("JsonWebToken", jwtPrincipal);
        return super.commit();
    }

    /**
     * Validate the bearer token passed in with the authorization header
     *
     * @param jwtCredential - the input bearer token
     * @return return the validated JWTCallerPrincipal
     * @throws ParseException - thrown on token parse or validation failure
     */
    protected JWTCallerPrincipal validate(JWTCredential jwtCredential) throws ParseException {
        JWTCallerPrincipalFactory factory = JWTCallerPrincipalFactory.instance();
        JWTCallerPrincipal callerPrincipal = factory.parse(jwtCredential.getBearerToken(), jwtCredential.getAuthContextInfo());
        return callerPrincipal;
    }

    @Override
    protected Group[] getRoleSets() throws LoginException {
        if (options.containsKey("rolesProperties")) {
            return super.getRoleSets();
        } else {
            Principal group =  subject.getPrincipals().stream()
                .filter(p -> p instanceof Group && "Roles".equals(p.getName())).findFirst().get();
            return new Group[] {(Group)group};
        }
    }
}
