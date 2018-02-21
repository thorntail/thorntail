package org.wildfly.swarm.container.config;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

public class EnvironmentConstructor extends Constructor {

    private final Map<String, String> environment;
    private final Pattern pattern = Pattern.compile("\\$\\{env.(.*?)\\}");

    public EnvironmentConstructor(Map<String, String> environment) {
        this.environment = environment;
        this.yamlConstructors.put(new Tag("!env"), new ConstructEnvironmentVariable());
    }

    Object getValue(String value) {
        Matcher matcher = pattern.matcher(value);
        if (environment != null && matcher.find()) {
           String variableName = matcher.group(1);
           String defaultValue = null;
           if (variableName.contains(":")) {
               String[] variableSplit = variableName.split(":",2);
               defaultValue = variableSplit[1];
               variableName = variableSplit[0];
           }
           String result = environment.get(variableName);
           if (result != null) {
               return result;
           } else if (defaultValue != null) {
               return defaultValue;
           }
        }
        return value;
    }

    private class ConstructEnvironmentVariable extends AbstractConstruct {
        public Object construct(Node node) {
            String value = (String) constructScalar((ScalarNode) node);
            return getValue(value);
        }
    }
}
