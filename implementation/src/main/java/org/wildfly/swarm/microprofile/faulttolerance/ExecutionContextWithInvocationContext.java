/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.microprofile.faulttolerance;

import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;


/**
 * @author Antoine Sabot-Durand
 */
class ExecutionContextWithInvocationContext implements ExecutionContext {

    public ExecutionContextWithInvocationContext(InvocationContext ic) {
        this.ic = ic;
    }

    @Override
    public Method getMethod() {
        return ic.getMethod();
    }

    @Override
    public Object[] getParameters() {
        return ic.getParameters();
    }

    public Object getTarget() {
        return ic.getTarget();
    }

    public Object proceed() throws Exception {
            return ic.proceed();
    }

    private InvocationContext ic;
}
