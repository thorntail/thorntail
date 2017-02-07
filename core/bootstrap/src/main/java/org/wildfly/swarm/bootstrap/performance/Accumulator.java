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
