package org.wildfly.swarm.spi.api.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A multi-segment configuration key.
 *
 * @author Bob McWhirter
 */
public class CompositeKey implements ConfigKey {

    public CompositeKey() {
    }

    public CompositeKey(SimpleKey... parts) {
        this.parts = Arrays.asList(parts);
    }

    public CompositeKey(String... parts) {
        this.parts = Arrays.asList(parts).stream().map(SimpleKey::new).collect(Collectors.toList());
    }

    public CompositeKey(CompositeKey parent, ConfigKey child) {
        this.parts = new ArrayList<>();
        this.parts.addAll(parent.parts);
        if (child == ConfigKey.EMPTY) {
            // skip
        } else if (child instanceof SimpleKey) {
            this.parts.add((SimpleKey) child);
        } else if (child instanceof CompositeKey) {
            this.parts.addAll(((CompositeKey) child).parts);
        }
    }

    public CompositeKey(CompositeKey parent, String child) {
        this(parent, new SimpleKey(child));
    }

    CompositeKey(List<SimpleKey> parts) {
        this.parts = parts;
    }

    @Override
    public boolean isChildOf(ConfigKey possibleParent) {
        if (possibleParent.head() == ConfigKey.EMPTY) {
            return true;
        }
        if (!this.head().equals(possibleParent.head())) {
            return false;
        }

        return this.subkey(1).isChildOf(possibleParent.subkey(1));
    }

    @Override
    public void replace(int position, String name) {
        this.parts.get(position).replace(0, name);
    }

    @Override
    public SimpleKey head() {
        if (this.parts.isEmpty()) {
            return ConfigKey.EMPTY;
        }

        return this.parts.get(0);
    }

    @Override
    public ConfigKey subkey(int offset) {
        if (this.parts.size() <= offset) {
            return ConfigKey.EMPTY;
        }

        List<SimpleKey> subParts = this.parts.subList(offset, this.parts.size());

        if (subParts.size() == 1) {
            return subParts.get(0);
        }

        return new CompositeKey(subParts);
    }

    @Override
    public String name() {
        return String.join(".",
                           this.parts.stream().map(SimpleKey::propertyName).collect(Collectors.toList()));
    }

    @Override
    public String propertyName() {
        return String.join(".",
                           this.parts.stream().map(SimpleKey::propertyName).collect(Collectors.toList()));
    }

    public CompositeKey append(String... names) {
        return append(ConfigKey.of(names));
    }

    public CompositeKey append(ConfigKey key) {
        return new CompositeKey(this, key);
    }

    @Override
    public int hashCode() {
        return this.parts.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof CompositeKey) {
            return this.parts.equals(((CompositeKey) obj).parts);
        }

        return false;
    }

    @Override
    public String toString() {
        return name();
    }

    private List<SimpleKey> parts;
}
