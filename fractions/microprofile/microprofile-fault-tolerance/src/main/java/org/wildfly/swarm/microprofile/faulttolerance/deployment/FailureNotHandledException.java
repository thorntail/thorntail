/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.faulttolerance.deployment;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

/**
 * This exception is thrown if a hystrix command has a fallback defined but the failure is not assignable from any failure exception listed in
 * {@link CircuitBreaker#failOn()}.
 *
 * @author Martin Kouba
 */
public class FailureNotHandledException extends RuntimeException {

    private static final long serialVersionUID = -4482803990615567626L;

    public FailureNotHandledException(Throwable cause) {
        super(cause);
    }

}
