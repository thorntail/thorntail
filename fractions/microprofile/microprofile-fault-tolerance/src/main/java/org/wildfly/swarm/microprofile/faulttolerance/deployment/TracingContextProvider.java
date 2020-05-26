/*
 * Copyright 2020 Red Hat, Inc, and individual contributors.
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

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

final class TracingContextProvider implements MiniConProp.ContextProvider {
    static final TracingContextProvider INSTANCE = new TracingContextProvider();

    public static boolean isRequired() {
        try {
            Class.forName("io.opentracing.Tracer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public MiniConProp.ContextSnapshot capture() {
        Tracer tracer = GlobalTracer.get();
        ScopeManager scopeManager = tracer.scopeManager();
        Scope activeScope = scopeManager.active();

        if (activeScope != null) {
            Span span = activeScope.span();
            return () -> {
                Scope propagated = scopeManager.activate(span, false);
                return propagated::close;
            };
        }

        return MiniConProp.ContextSnapshot.NOOP;
    }
}
