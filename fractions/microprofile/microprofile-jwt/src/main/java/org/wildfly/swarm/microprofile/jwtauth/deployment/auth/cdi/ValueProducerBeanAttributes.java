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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.util.AnnotationLiteral;

/**
 * The BeanAttributes for the ClaimValueProducer and JsonValueProducer
 */
public class ValueProducerBeanAttributes<T> implements BeanAttributes<T> {
    private final Set<Type> myTypes;

    private final Set<Annotation> myQualifiers;

    private final MPJWTExtension.ClaimIP claimIP;

    private Class<? extends Annotation> scope = RequestScoped.class;

    public ValueProducerBeanAttributes(Set<Type> myTypes, MPJWTExtension.ClaimIP claimIP) {
        this(myTypes, claimIP, RequestScoped.class);
    }

    public ValueProducerBeanAttributes(Set<Type> myTypes, MPJWTExtension.ClaimIP claimIP, Class<? extends Annotation> scope) {
        this.myTypes = myTypes;
        this.myQualifiers = new HashSet<>();
        this.claimIP = claimIP;
        this.myQualifiers.add(claimIP.getClaim());
        this.myQualifiers.add(new AnnotationLiteral<Any>() {
        });
        this.scope = scope;
    }

    /**
     * Set the producer method bean name to the claim name + the injection site type
     *
     * @return producer method bean name
     */
    @Override
    public String getName() {
        String ext = claimIP.isJsonValue() ? "json" : "CV";
        return null; //String.format("%s-%s[%s]", claimIP.getClaimName(), claimIP.getMatchType().getTypeName(), ext);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return myQualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public Set<Type> getTypes() {
        return myTypes;
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("ClaimValueProducer[%s]", getName());
    }

}
