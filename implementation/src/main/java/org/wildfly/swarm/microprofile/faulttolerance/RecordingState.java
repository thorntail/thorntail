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

import java.util.concurrent.atomic.AtomicInteger;

abstract class RecordingState extends State {

    protected final AtomicInteger successCount;

    protected final AtomicInteger failureCount;

    public RecordingState(SynchronousCircuitBreaker circuit) {
        super(circuit);
        this.successCount = new AtomicInteger(0);
        this.failureCount = new AtomicInteger(0);
    }

    @Override
    synchronized void onFailure() {
        failureCount.incrementAndGet();
        checkThreshold();
    }

    @Override
    synchronized void onSuccess() {
        successCount.incrementAndGet();
        checkThreshold();
    }

    int getRequestCount() {
        return failureCount.get() + successCount.get();
    }

    protected abstract void checkThreshold();

}
