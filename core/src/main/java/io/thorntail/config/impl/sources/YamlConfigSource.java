package io.thorntail.config.impl.sources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.thorntail.impl.CoreMessages;
import org.yaml.snakeyaml.Yaml;

/**
 * Created by bob on 2/5/18.
 */
public class YamlConfigSource extends MapConfigSource {

    public static YamlConfigSource of(URL url, int defaultOrdinal) throws IOException {
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
        } catch (ClassNotFoundException e) {
            CoreMessages.MESSAGES.unableToProcessYaml(url.toExternalForm());
            return null;
        }
        Yaml yaml = new Yaml();
        try (InputStream in = url.openStream()) {
            return new YamlConfigSource(url.toExternalForm(), yaml.load(in), defaultOrdinal);
        }
    }

    YamlConfigSource(String name, Map<String, ?> tree, int defaultOrdinal) {
        super(name, flatten(tree), defaultOrdinal);
    }

    static Map<String, String> flatten(Map<String, ?> tree) {
        Map<String, String> map = new HashMap<>();
        flatten(map, null, tree);
        return map;
    }

    static void flatten(Map<String, String> map, String prefix, Map<String, ?> tree) {
        for (Map.Entry<String, ?> entry : tree.entrySet()) {
            String name = keyOf(prefix, entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatten(map, name, (Map<String, ?>) value);
            } else if (value instanceof Collection) {
                map.put(name, escapeAndJoin((Collection<?>) value));
            } else {
                map.put(name, value.toString());
            }
        }
    }

    static String keyOf(String prefix, String chunk) {
        if (prefix == null) {
            return chunk;
        }

        return prefix + "." + chunk;
    }

    static String escapeAndJoin(Collection<?> data) {
        return data.stream()
                .map(e -> e.toString().replace(",", "\\,"))
                .collect(Collectors.joining(","));

    }

}
