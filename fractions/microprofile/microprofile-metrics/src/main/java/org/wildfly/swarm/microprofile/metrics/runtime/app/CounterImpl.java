/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *******************************************************************************
 * Copyright 2010-2013 Coda Hale and Yammer, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.metrics.runtime.app;

import java.util.concurrent.atomic.LongAdder;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.HitCounter;
import org.eclipse.microprofile.metrics.ParallelCounter;

/**
 * @author hrupp
 */
public class CounterImpl implements Counter, HitCounter, ParallelCounter {

    private final LongAdder count;

    public CounterImpl() {
        count = new LongAdder();
    }

    @Override
    public void inc() {
        count.increment();
    }

    @Override
    public void inc(long n) {
        count.add(n);
    }

    @Override
    public void dec() {
        count.decrement();
    }

    @Override
    public void dec(long n) {
        count.add(-n);
    }

    @Override
    public long getCount() {
        return count.sum();
    }
}
