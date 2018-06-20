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
 *
 */
package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.wildfly.swarm.microprofile.jwtauth.deployment.principal.DefaultJWTCallerPrincipal;


public class JWTBean implements Bean<JsonWebToken>, PassivationCapable {
    private Set<Type> types;
    private Set<Annotation> qualifiers;
    private Class<? extends Annotation> scope = RequestScoped.class;

    JWTBean() {
        types = new HashSet<>();
        types.add(JsonWebToken.class);
        types.add(Principal.class);
        types.add(Object.class);
        qualifiers = new HashSet<>();
        qualifiers.add(new AnnotationLiteral<Any>() { });
        qualifiers.add(new AnnotationLiteral<Default>() { });
    }
    @Override
    public Class<?> getBeanClass() {
        return DefaultJWTCallerPrincipal.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public JsonWebToken create(CreationalContext<JsonWebToken> creationalContext) {
        return MPJWTProducer.getJWTPrincpal();
    }

    @Override
    public void destroy(JsonWebToken instance, CreationalContext<JsonWebToken> creationalContext) {

    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public String getName() {
        return "JsonWebToken";
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String getId() {
        return "JsonWebToken";
    }
}
