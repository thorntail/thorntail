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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.DeploymentException;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.inject.Provider;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.JWTAuthMechanism;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.config.JWTAuthContextInfoProvider;

import io.smallrye.jwt.auth.cdi.ClaimValueProducer;
import io.smallrye.jwt.auth.cdi.CommonJwtProducer;
import io.smallrye.jwt.auth.cdi.JsonValueProducer;
import io.smallrye.jwt.auth.cdi.PrincipalProducer;
import io.smallrye.jwt.auth.cdi.RawClaimTypeProducer;

/**
 * A CDI extension that provides a producer for the current authenticated JsonWebToken based on a thread
 * local value that is managed by the {@link JWTAuthMechanism} request
 * authentication handler.
 *
 * This also installs the producer methods for the discovered:
 * <ul>
 * <li>@Claim ClaimValue<T> injection sites.</li>
 * <li>@Claim raw type<T> injection sites.</li>
 * <li>@Claim JsonValue injection sites.</li>
 * </ul>
 *
 * @see JWTAuthMechanism
 */
public class MPJWTExtension implements Extension {
    private static Logger log = Logger.getLogger(MPJWTExtension.class);

    /**
     * Register the MPJWTProducer JsonWebToken producer bean
     *
     * @param bbd         before discovery event
     * @param beanManager cdi bean manager
     */
    public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager beanManager) {
        log.debugf("MPJWTExtension(), adding producers");
        String extensionName = MPJWTExtension.class.getName();
        for (Class<?> clazz : new Class<?>[] {
                JWTAuthContextInfoProvider.class,
                CommonJwtProducer.class,
                PrincipalProducer.class,
                RawClaimTypeProducer.class,
                ClaimValueProducer.class,
                JsonValueProducer.class,
        }) {
            bbd.addAnnotatedType(beanManager.createAnnotatedType(clazz), extensionName + "_" + clazz.getName());
        }
    }

    /**
     * Replace the general producer method BeanAttributes with one bound to the collected injection site
     * types to properly reflect all of the type locations the producer method applies to.
     *
     * @param pba the ProcessBeanAttributes
     * @see ClaimProviderBeanAttributes
     */
    public void addTypeToClaimProducer(@Observes ProcessBeanAttributes pba) {
        if (!providerOptionalTypes.isEmpty() && pba.getAnnotated().isAnnotationPresent(Claim.class)) {
            Claim claim = pba.getAnnotated().getAnnotation(Claim.class);
            if (claim.value().length() == 0 && claim.standard() == Claims.UNKNOWN) {
                log.debugf("addTypeToClaimProducer: %s\n", pba.getAnnotated());
                BeanAttributes delegate = pba.getBeanAttributes();
                if (delegate.getTypes().contains(Optional.class)) {
                    pba.setBeanAttributes(new ClaimProviderBeanAttributes(delegate, providerOptionalTypes, providerQualifiers));
                }
            }
        }
    }

    /**
     * Collect the types of all {@linkplain Provider} injection points annotated with {@linkplain Claim}.
     *
     * @param pip - the injection point event information
     */
    void processClaimProviderInjections(@Observes ProcessInjectionPoint<?, ? extends Provider> pip) {
        log.debugf("pip: %s", pip.getInjectionPoint());
        final InjectionPoint ip = pip.getInjectionPoint();
        if (ip.getAnnotated().isAnnotationPresent(Claim.class)) {
            Claim claim = ip.getAnnotated().getAnnotation(Claim.class);
            if (claim.value().length() == 0 && claim.standard() == Claims.UNKNOWN) {
                pip.addDefinitionError(new DeploymentException("@Claim at: " + ip + " has no name or valid standard enum setting"));
            }
            boolean usesEnum = claim.standard() != Claims.UNKNOWN;
            final String claimName = usesEnum ? claim.standard().name() : claim.value();
            log.debugf("Checking Provider Claim(%s), ip: %s", claimName, ip);
            Type matchType = ip.getType();
            // The T from the Provider<T> injection site
            Type actualType = ((ParameterizedType) matchType).getActualTypeArguments()[0];
            // Don't add Optional or JsonValue as this is handled specially
            if (isOptional(actualType)) {
                // Validate that this is not an Optional<JsonValue>
                Type innerType = ((ParameterizedType) actualType).getActualTypeArguments()[0];
                if (!isJson(innerType)) {
                    providerOptionalTypes.add(actualType);
                    providerQualifiers.add(claim);
                }
            }
        }
    }

    private static boolean isOptional(Type type) {
        return type.getTypeName().startsWith(Optional.class.getTypeName());
    }

    private static boolean isJson(Type type) {
        return type.getTypeName().startsWith("javax.json.Json");
    }

    private Set<Type> providerOptionalTypes = new HashSet<>();
    private Set<Annotation> providerQualifiers = new HashSet<>();

}
