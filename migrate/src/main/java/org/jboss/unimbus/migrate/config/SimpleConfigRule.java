package org.jboss.unimbus.migrate.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.migrate.Action;

/**
 * Created by bob on 3/13/18.
 */
@ApplicationScoped
public class SimpleConfigRule implements ConfigRule {

    public SimpleConfigRule() throws IOException {
        this.mappings = new Properties();
        this.mappings.load(getClass().getClassLoader().getResourceAsStream("config-mappings.properties"));
    }

    @Override
    public List<? extends Action<Map, Properties>> match(Map context) {
        Set<String> names = this.mappings.stringPropertyNames();

        List<SimpleConfigAction> matches = new ArrayList<>();

        for (String name : names) {
            Object value = findValue(context, name);
            if (value != null) {
                matches.add(new SimpleConfigAction(this, this.mappings.getProperty(name), value));
            }
        }

        return matches;
    }

    protected Object findValue(Map context, String name) {
        return findValue(context, Arrays.asList(name.split("\\.")) );
    }

    protected Object findValue(Map context, List<String> parts) {
        if ( parts.isEmpty() ) {
            return null;
        }

        if ( ! context.containsKey( parts.get(0))) {
            return null;
        }

        Object next = context.get(parts.get(0));
        if ( next instanceof Map ) {
            return findValue((Map) next, parts.subList(1, parts.size()));
        }

        if ( parts.size() == 1 ) {
            return next;
        }

        return null;
    }


    private final Properties mappings;
}
