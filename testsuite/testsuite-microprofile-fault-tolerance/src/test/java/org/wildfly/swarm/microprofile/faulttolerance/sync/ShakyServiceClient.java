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
package org.wildfly.swarm.microprofile.faulttolerance.sync;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

public class ShakyServiceClient {

    static final AtomicInteger COUNTER = new AtomicInteger();

    static final int REQUEST_THRESHOLD = 2;

    static final long DELAY = 400;

    // successThreshold is ignored
    @CircuitBreaker(requestVolumeThreshold = REQUEST_THRESHOLD, delay = DELAY, successThreshold = 2)
    void ping(boolean success) {
        COUNTER.incrementAndGet();
        if (!success) {
            throw new IllegalStateException("Service call failed!");
        }
    }

}
