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
package org.wildfly.swarm.microprofile.faulttolerance.deployment.bulkhead.reject;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.TestArchive;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class BulkheadFallbackRejectTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        // See also src/test/resources/config.properties
        return TestArchive.createBase(BulkheadFallbackRejectTest.class).addPackage(BulkheadFallbackRejectTest.class.getPackage());
    }

    static final int QUEUE_SIZE = 20;

    @Test
    public void testFallbackNotRejected(PingService pingService) throws InterruptedException, ExecutionException {

        ExecutorService executorService = Executors.newFixedThreadPool(QUEUE_SIZE);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 1; i <= QUEUE_SIZE; i++) {
                tasks.add(() -> pingService.ping());
            }
            List<Future<String>> futures = executorService.invokeAll(tasks);
            for (Future<String> future : futures) {
                assertThat(future.get(), anyOf(equalTo("fallback"), equalTo("pong")));
            }
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

}