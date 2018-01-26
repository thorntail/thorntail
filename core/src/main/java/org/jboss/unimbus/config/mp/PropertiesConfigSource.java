package org.jboss.unimbus.config.mp;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class PropertiesConfigSource extends MapConfigSource {

    PropertiesConfigSource(String name, Properties props) {
        super(name,
              toMap(props));
    }

    private static Map<String, String> toMap(Properties props) {
        Map<String, String> map = new HashMap<>();
        props.stringPropertyNames()
                .forEach(name -> {
                    map.put(name, props.getProperty(name));
                });
        return map;
    }
}
