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

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.util.function.Supplier;

/**
 * @author Antoine Sabot-Durand
 */
public class DefaultCommand<R> extends com.netflix.hystrix.HystrixCommand<R> {


    protected DefaultCommand(Setter setter, Supplier<R> toRun, Supplier<R> fallback) {
        super(setter);
        this.toRun = toRun;
        this.fallback = fallback;
    }


    @Override
    protected R run() throws Exception {
        return toRun.get();
    }

    @Override
    protected R getFallback() {
        if (fallback == null) {
            return super.getFallback();
        }
        return fallback.get();
    }


    private final Supplier<R> fallback;

    private final Supplier<R> toRun;
}
