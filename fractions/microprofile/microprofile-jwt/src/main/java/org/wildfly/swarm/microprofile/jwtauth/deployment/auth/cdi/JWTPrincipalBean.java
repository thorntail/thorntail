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


public class JWTPrincipalBean implements Bean<Principal>, PassivationCapable {
    private Set<Type> types;
    private Set<Annotation> qualifiers;
    private Class<? extends Annotation> scope = RequestScoped.class;

    JWTPrincipalBean() {
        types = new HashSet<>();
        types.add(JsonWebToken.class);
        types.add(Object.class);
        qualifiers = new HashSet<>();
        qualifiers.add(new AnnotationLiteral<Any>() { });
        qualifiers.add(new AnnotationLiteral<Default>() { });
    }
    @Override
    public Class<?> getBeanClass() {
        return JsonWebToken.class;
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
    public Principal create(CreationalContext<Principal> creationalContext) {
        return MPJWTProducer.getJWTPrincpal();
    }

    @Override
    public void destroy(Principal instance, CreationalContext<Principal> creationalContext) {

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
        return "JsonWebToken+Principal";
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return true;
    }

    @Override
    public String getId() {
        return "JsonWebToken+Principal";
    }
}
