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
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class Accumulator {

    private final String description;

    private List<TimedEvent> children = new ArrayList<>();

    Accumulator(String description) {
        this.description = description;
    }

    TimedEvent newChild() {
        TimedEvent event = new TimedEvent();
        this.children.add(event);
        return event;
    }

    public void dump(long l, StringBuilder str) {
        long ms = this.children.stream()
                .collect(Collectors.summingLong(e -> e.durationMs()));

        str.append(String.format("%-80s...%s", this.description, Performance.formatTime(ms)));
        str.append("\n");
    }
}
