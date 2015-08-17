package org.wildfly.swarm.logstash.runtime;

import java.util.Collections;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.logstash.LogstashFraction;

/**
 * @author Bob McWhirter
 */
public class LogstashConfiguration extends AbstractServerConfiguration<LogstashFraction> {

    public LogstashConfiguration() {
        super(LogstashFraction.class);
    }

    @Override
    public boolean isIgnorable() {
        return true;
    }

    @Override
    public LogstashFraction defaultFraction() {
        return (LogstashFraction) LogstashFraction.createDefaultLogstashFraction(false);
    }

    @Override
    public List<ModelNode> getList(LogstashFraction fraction) {
        return Collections.emptyList();
    }

}
