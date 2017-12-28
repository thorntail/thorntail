/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.config.tck;

import java.util.List;

import javax.enterprise.inject.spi.DefinitionException;
import javax.enterprise.inject.spi.DeploymentException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;

/**
 *
 * @author Martin Kouba
 */
public class SwarmDeploymentExceptionTransformer implements DeploymentExceptionTransformer {

    private static final String[] DEPLOYMENT_EXCEPTION_FRAGMENTS = new String[] { "org.jboss.weld.exceptions.DeploymentException",
            "org.jboss.weld.exceptions.UnserializableDependencyException", "org.jboss.weld.exceptions.InconsistentSpecializationException",
            "org.jboss.weld.exceptions.NullableDependencyException" };

    private static final String[] DEFINITION_EXCEPTION_FRAGMENTS = new String[] { "org.jboss.weld.exceptions.DefinitionException" };

    public Throwable transform(Throwable throwable) {

        // Arquillian sometimes returns InvocationException with nested
        // exception and sometimes exception itself
        @SuppressWarnings("unchecked")
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(throwable);
        if (throwableList.isEmpty())
            return throwable;

        Throwable root = null;

        if (throwableList.size() == 1) {
            root = throwable;
        } else {
            root = ExceptionUtils.getRootCause(throwable);
        }

        if (root instanceof DeploymentException || root instanceof DefinitionException) {
            return root;
        }
        if (isFragmentFound(DEPLOYMENT_EXCEPTION_FRAGMENTS, root)) {
            return new DeploymentException(root.getMessage());
        }
        if (isFragmentFound(DEFINITION_EXCEPTION_FRAGMENTS, root)) {
            return new DefinitionException(root.getMessage());
        }
        return throwable;
    }

    private boolean isFragmentFound(String[] fragments, Throwable rootException) {
        for (String fragment : fragments) {
            if (rootException.getMessage().contains(fragment)) {
                return true;
            }
        }
        return false;
    }

}
