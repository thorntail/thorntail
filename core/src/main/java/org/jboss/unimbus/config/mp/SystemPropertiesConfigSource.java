package org.jboss.unimbus.config.mp;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class SystemPropertiesConfigSource implements ConfigSource {

    @Override
    public Map<String, String> getProperties() {
        return toMap(System.getProperties());
    }

    @Override
    public String getValue(String propertyName) {
        return System.getProperty(propertyName);
    }

    @Override
    public String getName() {
        return "system-properties";
    }

    protected static Map<String,String> toMap(Properties props) {
        Map<String,String> map = new HashMap<>();

        System.getProperties().stringPropertyNames().forEach(e->{
            map.put( e, System.getProperty(e));
        });

        return map;
    }


}
