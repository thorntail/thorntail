package org.wildfly.swarm.keycloak.internal;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.adapters.config.BaseAdapterConfig;
import org.keycloak.representations.adapters.config.BaseRealmConfig;

class KeycloakJsonGenerator {

    static final String PREFIX = "swarm.keycloak.adapter-config.";

    private static final Set<String> REQUESTED_ADAPTER_CONFIGS;

    static {
        REQUESTED_ADAPTER_CONFIGS = System.getProperties().keySet().stream()
                .map(Object::toString)
                .filter(key -> key.startsWith(PREFIX))
                .map(key -> key.substring(PREFIX.length()))
                .collect(Collectors.toSet());
    }

    static InputStream generate() {
        byte[] keycloakJson = null;
        try {
            keycloakJson = new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writeValueAsBytes(setupAdapterConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(keycloakJson);
    }

    private static AdapterConfig setupAdapterConfig() throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        AdapterConfig adapterConfig = new AdapterConfig();

        for (Map.Entry<String, Field> config : populateAllAdapterConfigs().entrySet()) {
            String json = config.getKey();
            Field field = config.getValue();

            PropertyDescriptor property = new PropertyDescriptor(field.getName(), adapterConfig.getClass());
            set(adapterConfig, json, field.getType(), property.getWriteMethod());
        }

        return adapterConfig;
    }

    private static Map<String, Field> populateAllAdapterConfigs() {
        Map<String, Field> allAdapterConfigs = new HashMap<>();

        Arrays.asList(BaseRealmConfig.class, BaseAdapterConfig.class, AdapterConfig.class).forEach(config -> {
            for (Field field : config.getDeclaredFields()) {
                allAdapterConfigs.put(field.getAnnotation(JsonProperty.class).value(), field);
            }
        });

        return allAdapterConfigs;
    }

    private static void set(AdapterConfig adapterConfig, String json, Class<?> type, Method setter) throws IllegalAccessException, InvocationTargetException {
        if (! REQUESTED_ADAPTER_CONFIGS.contains(json)) {
            return;
        }

        if (type == boolean.class || type == Boolean.class) {
            setter.invoke(adapterConfig, Boolean.valueOf(System.getProperty(PREFIX + json)));
            return;
        }

        if (type == int.class  || type == Integer.class) {
            setter.invoke(adapterConfig, Integer.valueOf(System.getProperty(PREFIX + json)));
            return;
        }

        if (type == Map.class) {
            String[] pairs = System.getProperty(PREFIX + json).split(",");
            Map<String, Object> map = new HashMap<>();
            for (String pair : pairs) {
                map.put(pair.split("=")[0].trim(), pair.split("=")[1].trim());
            }
            setter.invoke(adapterConfig, map);
            return;
        }

        setter.invoke(adapterConfig, System.getProperty(PREFIX + json));
    }

}
