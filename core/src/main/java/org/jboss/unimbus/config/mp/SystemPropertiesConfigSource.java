package org.jboss.unimbus.config.mp;

import java.util.Properties;

public class SystemPropertiesConfigSource extends PropertiesConfigSource {
    SystemPropertiesConfigSource() {
        super("system-properties", System.getProperties());
    }
}
