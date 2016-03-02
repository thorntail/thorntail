/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.batch.jberet;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemReader;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Named
public class CountingItemReader implements ItemReader {

    @Override
    public void open(final Serializable checkpoint) throws Exception {
        if (checkpoint != null) {
            counter.set((Integer) checkpoint);
        } else if (start != null) {
            counter.set(Integer.parseInt(start));
        }
        if (end == null) {
            endCount = 10;
        } else {
            endCount = Integer.parseInt(end);
        }
    }

    @Override
    public void close() throws Exception {
        counter.set(0);
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return counter.get();
    }

    @Override
    public Object readItem() throws Exception {
        final int current = counter.getAndIncrement();
        if (current < endCount)
            return current;
        return null;
    }

    private final AtomicInteger counter = new AtomicInteger();

    @BatchProperty(name = "chunk.start")
    @Inject
    private String start;

    @BatchProperty(name = "chunk.end")
    @Inject
    private String end;

    private volatile int endCount;
}
