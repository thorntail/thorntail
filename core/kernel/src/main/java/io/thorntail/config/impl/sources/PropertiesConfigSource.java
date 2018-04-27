package io.thorntail.config.impl.sources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesConfigSource extends MapConfigSource {

    public static PropertiesConfigSource of(URL url, int defaultOrdinal) throws IOException {
        try (InputStream in = url.openStream()) {
            Properties props = new Properties();
            props.load(in);
            return new PropertiesConfigSource(url.toExternalForm(), props, defaultOrdinal);
        }
    }

    PropertiesConfigSource(String name, Properties props, int defaultOrdinal) {
        super(name,
              toMap(props),
              defaultOrdinal);
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
