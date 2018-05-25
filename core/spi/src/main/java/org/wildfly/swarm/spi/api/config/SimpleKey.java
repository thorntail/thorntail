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
 * @author Bob McWhirter
 */
public class SimpleKey implements ConfigKey {

    SimpleKey() {
        this.name = null;
    }

    public SimpleKey(String name) {
        this.name = name;
    }

    public String name() {
        if (this == ConfigKey.EMPTY) {
            return "";
        }
        return this.name;
    }

    public String propertyName() {
        if (!this.name.contains(".")) {
            return this.name;
        }

        return START_DELIM + this.name + END_DELIM;
    }

    @Override
    public SimpleKey head() {
        return this;
    }

    @Override
    public ConfigKey subkey(int offset) {
        if (offset == 0) {
            return this;
        }

        return ConfigKey.EMPTY;
    }

    @Override
    public boolean isChildOf(ConfigKey possibleParent) {
        if (this == ConfigKey.EMPTY) {
            return true;
        }

        if (possibleParent == ConfigKey.EMPTY) {
            return true;
        }

        return false;
    }

    @Override
    public void replace(int position, String name) {
        if (this == ConfigKey.EMPTY) {
            throw new RuntimeException("Cannot replace an empty key");
        }

        if (position != 0) {
            throw new IndexOutOfBoundsException("Cannot replace position: " + position);
        }

        this.name = name;
    }

    @Override
    public ConfigKey append(ConfigKey key) {
        if (this == ConfigKey.EMPTY) {
            return key;
        }
        if (key == ConfigKey.EMPTY) {
            return this;
        }
        return new CompositeKey(this.name).append(key);
    }

    @Override
    public ConfigKey append(String... names) {
        ConfigKey cur = this;

        for (String each : names) {
            cur = cur.append(new SimpleKey(each));
        }
        return cur;
    }

    @Override
    public int hashCode() {
        if (this == ConfigKey.EMPTY) {
            return System.identityHashCode(this);
        }
        return this.name.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == ConfigKey.EMPTY) {
            return obj == ConfigKey.EMPTY;
        }

        if (obj instanceof SimpleKey) {
            return this.name.equalsIgnoreCase((((SimpleKey) obj).name));
        }

        return false;
    }

    @Override
    public String toString() {
        if (this == ConfigKey.EMPTY) {
            return "(empty)";
        }
        return name();
    }

    private String name;

}
