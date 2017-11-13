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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;


/**
 * @author Antoine Sabot-Durand
 */
public class FutureStringFallbackHandler implements FallbackHandler<Future<String>> {

    @Override
    public Future<String> handle(ExecutionContext executionContext) {
        return FALLBACK;
    }

    static void reset() {
        DISPOSED.set(false);
    }

    @PreDestroy
    void dispose() {
        DISPOSED.set(shared.ping());
    }

    static final Future<String> FALLBACK = CompletableFuture.completedFuture("Store is closed");

    static final AtomicBoolean DISPOSED = new AtomicBoolean(false);

    @Inject
    SharedFallback shared;

}
