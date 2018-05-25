/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wildfly.swarm.swagger.deployment;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Application;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.wildfly.swarm.swagger.SwaggerMessages;

/**
 * The {@code SwaggerRestApplicationInterceptor} gets applied to JAX-RS Application subtypes and ensures that the Swagger
 * resources get included as part of the Application deployment.
 */
@AddSwaggerResources
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 200)
public class SwaggerRestApplicationInterceptor {

    /**
     * As per the JAX-RS specification, if a deployment sub-classes JAX-RS Application and returns a non-empty collection for
     * either {@link Application#getClasses()} or {@link Application#getSingletons()}, then, only the references mentioned in
     * those collections should be used as REST resources. This poses a slight problem when the developers <i>expect</i> to see
     * their Swagger resources, but don't see it (due to specification conformance). This method takes care of adding the
     * relevant resources (if required).
     */
    @SuppressWarnings("unchecked")
    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        Object response = context.proceed();

        // Verify if we need to do anything at all or not. This is to avoid the potential misconfiguration where this
        // interceptor gets added to beans that should not be included.
        Method method = context.getMethod();
        if (Application.class.isAssignableFrom(method.getDeclaringClass())) {
            if ("getClasses".equals(method.getName())) {
                Set<Class<?>> classes = new HashSet<>((Set<Class<?>>) response);

                // Check the response for singletons as well.
                Method getSingletons = Application.class.getDeclaredMethod("getSingletons");
                Set singletons = (Set) getSingletons.invoke(context.getTarget());
                if (!classes.isEmpty() || !singletons.isEmpty()) {
                    classes.add(ApiListingResource.class);
                    classes.add(SwaggerSerializers.class);
                    response = classes;
                    SwaggerMessages.MESSAGES.addingSwaggerResourcesToCustomApplicationSubClass();
                }
            }
        } else {
            SwaggerMessages.MESSAGES.warnInvalidBeanTarget(method.getDeclaringClass());
        }

        return response;
    }
}
