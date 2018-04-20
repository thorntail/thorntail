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
package org.wildfly.swarm.microprofile.faulttolerance.deployment.bulkhead;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;

public class PingService {

    @Asynchronous
    @Bulkhead(value = BulkheadTest.QUEUE_SIZE, waitingTaskQueue = BulkheadTest.QUEUE_SIZE)
    public Future<String> ping(CountDownLatch startLatch, CountDownLatch endLatch) throws InterruptedException {
        if (startLatch != null) {
            startLatch.countDown();
        }
        if (endLatch != null) {
            if (endLatch.await(1, TimeUnit.SECONDS)) {
                return CompletableFuture.completedFuture("pong");
            } else {
                return CompletableFuture.completedFuture("timeout");
            }
        }
        return CompletableFuture.completedFuture("null");
    }

}
