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
package org.wildfly.swarm.microprofile.faulttolerance.fallbackmethod.nonpublic;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.Dependent;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;

@Dependent
public class FaultyService {

    static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Fallback(fallbackMethod = "fallback")
    @Retry(maxRetries = 2)
    public int foo() {
        COUNTER.incrementAndGet();
        throw new IllegalStateException();
    }

    int fallback() {
        return 1;
    }

    @Fallback(fallbackMethod = "fallbackParameterized")
    @Retry(maxRetries = 2)
    public List<String> fooParameterized() {
        COUNTER.incrementAndGet();
        throw new IllegalStateException();
    }

    List<String> fallbackParameterized() {
        return Collections.emptyList();
    }

}
