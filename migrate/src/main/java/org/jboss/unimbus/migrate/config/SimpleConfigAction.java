package org.jboss.unimbus.migrate.config;

import java.util.Map;
import java.util.Properties;

import org.jboss.unimbus.migrate.Action;
import org.jboss.unimbus.migrate.Rule;

/**
 * Created by bob on 3/13/18.
 */
public class SimpleConfigAction implements Action<Map, Properties> {

    public SimpleConfigAction(SimpleConfigRule rule, String name, Object value) {
        this.rule = rule;
        this.name = name;
        this.value = value;
    }

    @Override
    public Rule<Map, Properties> getRule() {
        return this.rule;
    }

    @Override
    public void apply(Properties properties) {
        properties.setProperty(this.name, this.value.toString());
    }

    private final SimpleConfigRule rule;

    private final String name;

    private final Object value;
}
