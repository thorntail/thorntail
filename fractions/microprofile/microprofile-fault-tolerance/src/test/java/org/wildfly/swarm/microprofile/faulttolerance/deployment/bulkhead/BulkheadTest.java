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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
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
public class BulkheadTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return TestArchive.createBase(BulkheadTest.class).addPackage(BulkheadTest.class.getPackage());
    }

    static final int QUEUE_SIZE = 3;

    @Test
    public void testWaitingQueue(PingService pingService) throws InterruptedException, ExecutionException {
        int loop = QUEUE_SIZE * 2;
        CountDownLatch startLatch = new CountDownLatch(loop);
        CountDownLatch endLatch = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < loop; i++) {
            futures.add(pingService.ping(startLatch, endLatch));
        }
        startLatch.await(500, TimeUnit.MILLISECONDS);
        // Next invocation should not make it due to BulkheadException
        try {
            pingService.ping(null, null).get();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof BulkheadException);
        }
        endLatch.countDown();
        for (int i = 0; i < loop; i++) {
            assertEquals("pong", futures.get(i).get());
        }
    }

}