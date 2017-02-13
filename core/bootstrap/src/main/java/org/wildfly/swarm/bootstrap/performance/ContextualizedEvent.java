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
