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
package org.wildfly.swarm.microprofile.jwtauth;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 5/30/18
 */
public class MpJwtFilterRegistrar implements DynamicFeature {

    private static final DenyAllFilter denyAllFilter = new DenyAllFilter();
    private final Set<Class<? extends Annotation>> mpJwtAnnotations =
            new HashSet<>(asList(DenyAll.class, PermitAll.class, RolesAllowed.class));


    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        Annotation mpJwtAnnotation = getMpJwtAnnotation(resourceInfo);
        if (mpJwtAnnotation != null) {
            if (mpJwtAnnotation instanceof DenyAll) {
                configureDenyAll(context);
            } else if (mpJwtAnnotation instanceof RolesAllowed) {
                configureRolesAllowed((RolesAllowed) mpJwtAnnotation, context);
            }
        } else {
            // the resource method is not annotated and the class is not annotated either
            if (hasSecurityAnnotations(resourceInfo) && shouldNonannotatedMethodsBeDenied()) {
                // some other method has a security annotation and this one doesn't, it should be @DenyAll by default
                configureDenyAll(context);
            }
        }
    }

    private void configureRolesAllowed(RolesAllowed mpJwtAnnotation, FeatureContext context) {
        context.register(new RolesAllowedFilter(mpJwtAnnotation.value()));
    }

    private void configureDenyAll(FeatureContext context) {
        context.register(denyAllFilter);
    }

    private Annotation getMpJwtAnnotation(ResourceInfo resourceInfo) {
        Annotation annotation = getAnnotation(
                resourceInfo.getResourceMethod().getDeclaredAnnotations(),
                () -> resourceInfo.getResourceClass().getCanonicalName() + ":" + resourceInfo.getResourceMethod().getName()
        );
        if (annotation == null) {
            annotation = getAnnotation(resourceInfo.getResourceMethod().getDeclaringClass().getDeclaredAnnotations(),
                    () -> resourceInfo.getResourceClass().getCanonicalName());
        }

        return annotation;
    }

    private Annotation getAnnotation(Annotation[] declaredAnnotations,
                                     Supplier<String> annotationPlacementDescriptor) {
        List<Annotation> annotations = Stream.of(declaredAnnotations)
                .filter(annotation -> mpJwtAnnotations.contains(annotation.annotationType()))
                .collect(Collectors.toList());
        switch (annotations.size()) {
            case 0:
                return null;
            case 1:
                return annotations.iterator().next();
            default:
                throw new RuntimeException("Duplicate MicroProfile JWT annotations found on "
                        + annotationPlacementDescriptor.get() +
                        ". Expected at most 1 annotation, found: " + annotations);
        }
    }

    private boolean hasSecurityAnnotations(ResourceInfo resource) {
        // resource methods are inherited (see JAX-RS spec, chapter 3.6)
        // resource methods must be `public` (see JAX-RS spec, chapter 3.3.1)
        // hence `resourceClass.getMethods` -- returns public methods, including inherited ones
        return Stream.of(resource.getResourceClass().getMethods())
                .filter(this::isResourceMethod)
                .anyMatch(this::hasSecurityAnnotations);
    }

    private boolean hasSecurityAnnotations(Method method) {
        return Stream.of(method.getAnnotations())
                .anyMatch(annotation -> mpJwtAnnotations.contains(annotation.annotationType()));
    }

    private boolean isResourceMethod(Method method) {
        // resource methods are methods annotated with an annotation that is itself annotated with @HttpMethod
        // (see JAX-RS spec, chapter 3.3)
        return Stream.of(method.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().getAnnotation(HttpMethod.class) != null);
    }

    private boolean shouldNonannotatedMethodsBeDenied() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL resource = loader.getResource("/META-INF/MP-JWT-DENY-NONANNOTATED-METHODS");
        return resource != null;
    }
}
