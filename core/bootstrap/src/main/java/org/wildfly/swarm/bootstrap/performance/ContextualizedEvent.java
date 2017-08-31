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

/**
 * @author Bob McWhirter
 */
public class ContextualizedEvent implements AutoCloseable {

    ContextualizedEvent(String description) {
        this.description = description;
        this.event = new TimedEvent();
    }

    @Override
    public void close() throws Exception {
        this.event.close();
        Performance.CONTEXT_STACK.pop();
        if (Performance.CONTEXT_STACK.isEmpty()) {
            Performance.TOTAL_MEASURED_TIME.addAndGet(this.event.durationMs());
        }
    }

    ContextualizedEvent newChild(String description) {
        ContextualizedEvent child = new ContextualizedEvent(description);
        this.children.add(child);
        return child;
    }

    public void dump(String indent, long totalTime, StringBuilder str) {

        double percentage = (((double) this.event.durationMs() / (double) totalTime) * 100);
        String descFormat = "%-" + (80 - indent.length()) + "s";
        str.append(indent);
        str.append(String.format(descFormat + "...%s (%6.2f%%)", this.description, Performance.formatTime(this.event.durationMs()), percentage));
        str.append("\n");

        this.children.forEach(e -> {
            e.dump(indent + "  ", totalTime, str);
        });
    }

    private final String description;

    private final TimedEvent event;

    private List<ContextualizedEvent> children = new ArrayList<>();

}
