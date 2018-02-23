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

package org.wildfly.swarm.microprofile.faulttolerance.deployment;

import java.util.function.Supplier;

import com.netflix.hystrix.HystrixCommand;

/**
 * @author Antoine Sabot-Durand
 */
public class DefaultCommand extends HystrixCommand<Object> {

    /**
     *
     * @param setter
     * @param ctx
     * @param fallback
     */
    protected DefaultCommand(Setter setter, ExecutionContextWithInvocationContext ctx, Supplier<Object> fallback) {
        super(setter);
        this.ctx = ctx;
        this.fallback = fallback;
    }

    @Override
    protected Object run() throws Exception {
        return ctx.proceed();
    }

    @Override
    protected Object getFallback() {
        if (fallback == null) {
            return super.getFallback();
        }
        return fallback.get();
    }

    private final Supplier<Object> fallback;

    private final ExecutionContextWithInvocationContext ctx;

}
