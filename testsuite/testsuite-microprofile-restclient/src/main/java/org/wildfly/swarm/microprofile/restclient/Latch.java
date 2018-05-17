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
package org.wildfly.swarm.microprofile.restclient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Latch {

    private static final String DEFAULT_NAME = "default";

    private final Map<String, CountDownLatch> latches = new ConcurrentHashMap<>();

    public void countDown(String name) {
        CountDownLatch latch = latches.get(name);
        if (latch != null) {
            latch.countDown();
        }
    }

    public void countDown() {
        countDown(DEFAULT_NAME);
    }

    public boolean await(String name) throws InterruptedException {
        CountDownLatch latch = latches.get(name);
        return latch != null ? latch.await(30, TimeUnit.SECONDS) : false;
    }

    public boolean await() throws InterruptedException {
        return await(DEFAULT_NAME);
    }

    public void add(String name, int value) {
        latches.put(name, new CountDownLatch(value));
    }

    public void reset(int value) {
        latches.clear();
        add(DEFAULT_NAME, value);
    }

}
