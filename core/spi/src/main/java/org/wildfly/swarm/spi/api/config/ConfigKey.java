/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.spi.api.config;

/**
 * A key to configuration values.
 *
 * @author Bob McWhirter
 */
public interface ConfigKey {

    /**
     * String representation of this key's name.
     *
     * @return The name.
     */
    String name();


    /**
     * String representation of this key's name in property format.
     *
     * @return The name in property format.
     */
    String propertyName();

    /**
     * Return the first segment of this key.
     *
     * @return The first segment of this key.
     */
    SimpleKey head();

    /**
     * Return the subkey of this key from a given offset.
     *
     * @param offset The number of segments offset from the head to subkey.
     * @return The subkey or {@link #EMPTY}.
     */
    ConfigKey subkey(int offset);

    /**
     * Sentinal representing an empty key.
     */
    SimpleKey EMPTY = new SimpleKey();

    /**
     * Return a new key with the given segments appended.
     *
     * @param names The segments to append.
     * @return The new key.
     */
    ConfigKey append(String... names);

    /**
     * Return a new key with the given key appended.
     *
     * @param key The key to append.
     * @return The new key.
     */
    ConfigKey append(ConfigKey key);

    /**
     * Determine if this key is a child of the argument.
     *
     * @param possibleParent The possible parent key.
     * @return {@code true} if this key is a child of the argument, otherwise {@code false}.
     */
    boolean isChildOf(ConfigKey possibleParent);

    void replace(int position, String name);

    /**
     * Create a key from a series of names.
     *
     * @param parts The segments.
     * @return The new key.
     */
    static ConfigKey of(String... parts) {
        return new CompositeKey(parts);
    }

    char START_DELIM = '[';
    char END_DELIM = ']';

    /**
     * Parse a multi-segment key from a single string.
     *
     * @param str The string to parse.
     * @return The new key.
     */
    static ConfigKey parse(String str) {

        ConfigKey key = ConfigKey.EMPTY;

        if (str.startsWith("swarm.")) {
            str = str.replaceFirst("swarm.", "thorntail.");
        }

        int len = str.length();
        int i = 0;

        StringBuilder segment = new StringBuilder();

        LOOP:
        while (true) {
            if (i >= len) {
                break;
            }

            char c = str.charAt(i);

            switch (c) {
                case START_DELIM:
                    ++i;
                    while (true) {
                        if (i >= len) {
                            break LOOP;
                        }
                        c = str.charAt(i);
                        if (c == END_DELIM) {
                            break;
                        }
                        segment.append(c);
                        ++i;
                    }
                    break;
                case '.':
                    key = key.append(segment.toString());
                    segment = new StringBuilder();
                    break;
                default:
                    segment.append(c);
                    break;
            }

            ++i;
        }

        if (segment.length() > 0) {
            key = key.append(segment.toString());
        }

        return key;
    }
}
