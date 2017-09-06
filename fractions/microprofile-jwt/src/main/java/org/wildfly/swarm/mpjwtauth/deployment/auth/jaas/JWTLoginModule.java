/*
 * Copyright (c) 2016-2017 Contributors to the Eclipse Foundation
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.wildfly.swarm.mpjwtauth.deployment.auth.jaas;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.wildfly.swarm.mpjwtauth.deployment.principal.JWTCallerPrincipal;
import org.wildfly.swarm.mpjwtauth.deployment.principal.JWTCallerPrincipalFactory;
import org.wildfly.swarm.mpjwtauth.deployment.principal.ParseException;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.SecurityAssociationCallback;
import org.jboss.security.auth.spi.RoleMappingLoginModule;

public class JWTLoginModule extends RoleMappingLoginModule {
    private JsonWebToken jwtPrincipal;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);

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
            LoginException ex = new LoginException("Failed to obtain JWTCredential from SecurityAssociationCallback");
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
}
