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
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == ConfigKey.EMPTY) {
            return obj == ConfigKey.EMPTY;
        }

        if (obj instanceof SimpleKey) {
            return this.name.equals((((SimpleKey) obj).name));
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
