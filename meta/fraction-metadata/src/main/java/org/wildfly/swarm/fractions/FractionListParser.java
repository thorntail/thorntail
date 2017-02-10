package org.wildfly.swarm.fractions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Parses a JSON file and a Properties
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class FractionListParser {
    private final Map<String, FractionDescriptor> descriptors = new TreeMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public FractionListParser(InputStream fractionListJson) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(fractionListJson)) {
            Json.parse(reader).asArray().forEach(entry -> {
                JsonObject fraction = entry.asObject();
                FractionDescriptor fd = getFractionDescriptor(fraction);
                addDependencies(fraction, fd);
            });
        }
    }

    private void addDependencies(JsonObject fraction, FractionDescriptor parent) {
        fraction.get("fractionDependencies").asArray().forEach(entry -> {
            JsonObject dependency = entry.asObject();
            FractionDescriptor descriptor = getFractionDescriptor(dependency);
            if (parent != null) {
                parent.addDependency(descriptor);
            }
            addDependencies(dependency, descriptor);
        });
    }

    private FractionDescriptor getFractionDescriptor(JsonObject fraction) {
        String groupId = toString(fraction.get("groupId"));
        String artifactId = toString(fraction.get("artifactId"));
        String key = groupId + ":" + artifactId;
        FractionDescriptor descriptor = descriptors.get(key);
        if (descriptor == null) {
            String version = toString(fraction.get("version"));
            String name = toString(fraction.get("name"));
            String description = toString(fraction.get("description"));
            String tags = toString(fraction.get("tags"));
            boolean internal = toBoolean(fraction.get("internal"));

            JsonValue stabilityIndexJson = fraction.get("stabilityIndex");
            int stabilityIndex = stabilityIndexJson == null || stabilityIndexJson.isNull() ? FractionStability.UNSTABLE.ordinal() : stabilityIndexJson.asInt();
            FractionStability stability;
            if (stabilityIndex < 0 || stabilityIndex >= FractionStability.values().length) {
                stability = FractionStability.UNSTABLE;
            } else {
                stability = FractionStability.values()[stabilityIndex];
            }
            descriptor = new FractionDescriptor(groupId, artifactId, version, name, description, tags, internal, stability);
            descriptors.put(key, descriptor);
        }
        return descriptor;
    }

    private boolean toBoolean(JsonValue jsonValue) {
        return jsonValue.isNull() ? false : jsonValue.asBoolean();
    }

    private String toString(JsonValue jsonValue) {
        return jsonValue.isNull() ? null : jsonValue.asString();
    }

    public Collection<FractionDescriptor> getFractionDescriptors() {
        return Collections.unmodifiableCollection(this.descriptors.values());
    }

    public FractionDescriptor getFractionDescriptor(final String groupId, final String artifactId) {
        return this.descriptors.get(groupId + ":" + artifactId);
    }
}
