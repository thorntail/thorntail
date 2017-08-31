/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Bob McWhirter
 */
public class Performance {

    private static long START_TIME = -1;

    static Map<String, Accumulator> ACCUMULATORS = new HashMap<>();

    static Stack<ContextualizedEvent> CONTEXT_STACK = new Stack<>();

    static List<ContextualizedEvent> ROOT_CONTEXTS = new ArrayList<>();

    static AtomicLong TOTAL_MEASURED_TIME = new AtomicLong();

    private Performance() {
    }

    public static void start() {
        START_TIME = System.currentTimeMillis();
    }

    static String formatTime(long ms) {

        long s = 0;
        long m = 0;

        s = ms / 1000;
        ms = ms % 1000;

        m = s / 60;
        s = s % 60;

        return String.format("%02d:%02d.%03d", m, s, ms);
    }

    public static synchronized AutoCloseable time(String description) {
        ContextualizedEvent event = null;

        if (CONTEXT_STACK.isEmpty()) {
            event = new ContextualizedEvent(description);
            ROOT_CONTEXTS.add(event);
        } else {
            event = CONTEXT_STACK.peek().newChild(description);
        }

        CONTEXT_STACK.push(event);
        return event;
    }

    public static synchronized AutoCloseable accumulate(String description) {
        Accumulator accumulator = ACCUMULATORS.get(description);
        if (accumulator == null) {
            accumulator = new Accumulator(description);
            ACCUMULATORS.put(description, accumulator);
        }

        return accumulator.newChild();
    }

    public static String dump() {
        StringBuilder str = new StringBuilder();

        long totalTime = (START_TIME > 0 ? System.currentTimeMillis() - START_TIME : TOTAL_MEASURED_TIME.get());

        ROOT_CONTEXTS.forEach(e -> {
            e.dump("", totalTime, str);
        });

        ACCUMULATORS.values().forEach(e -> {
            e.dump(totalTime, str);
        });

        str.append("Total: " + formatTime(totalTime));

        return str.toString().trim();
    }
}
