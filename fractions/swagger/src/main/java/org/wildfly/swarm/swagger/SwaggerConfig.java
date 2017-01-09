/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public SwaggerConfig(InputStream input) {
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        String line;

        try {
            while ((line = in.readLine()) != null) {
                int separatorIndex = line.indexOf(":");
                Key key = Key.valueOf(line.substring(0, separatorIndex).toUpperCase());
                Object value = line.substring(separatorIndex + 1);

                // SCHEMES is meant to be a String[]
                // everything else is a String
                if (key == Key.SCHEMES || key == Key.PACKAGES) {
                    value = ((String) value).split(",");
                }
                put(key, value);
            }
        } catch (IllegalArgumentException ia) {
            throw new RuntimeException("Invalid key: " + ia.getMessage());
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

    private final HashMap<Key, Object> config = new HashMap<>();

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
}
