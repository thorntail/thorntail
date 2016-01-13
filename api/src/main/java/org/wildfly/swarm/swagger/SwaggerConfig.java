package org.wildfly.swarm.swagger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Lance Ball
 */
public class SwaggerConfig {

    public enum Key {
        TITLE,
        PACKAGES,
        DESCRIPTION,
        TERMS_OF_SERVICE_URL,
        CONTACT,
        LICENSE,
        LICENSE_URL,
        VERSION,
        SCHEMES,
        HOST,
        ROOT,
        PRETTY_PRINT
    }

    private final HashMap<Key, Object> config = new HashMap<>();

    public SwaggerConfig(InputStream input) {
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        String line;

        try {
            while((line = in.readLine()) != null) {
                int separatorIndex = line.indexOf(":");
                Key key = Key.valueOf(line.substring(0, separatorIndex));
                Object value = line.substring(separatorIndex+1);

                // SCHEMES is meant to be a String[]
                // everything else is a String
                if (key == Key.SCHEMES) {
                    value = ((String)value).split(",");
                }
                put(key, value);
            }
        } catch (IOException e) {
            System.err.println("ERROR reading SwaggerConfigurationAsset" + e);
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SwaggerConfig() {

    }

    public SwaggerConfig put(Key key, Object value) {
        config.put(key, value);
        return this;
    }

    public Object get(Key key) {
        return config.get(key);
    }

    public Set<Map.Entry<Key, Object>> entrySet() {
        return config.entrySet();
    }
}
