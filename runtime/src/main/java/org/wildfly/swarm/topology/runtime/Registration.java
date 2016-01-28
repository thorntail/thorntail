package org.wildfly.swarm.topology.runtime;

import java.io.Serializable;
import java.util.Arrays;

import org.wildfly.swarm.topology.Topology;

/**
 * @author Bob McWhirter
 */
public class Registration implements Topology.Entry, Serializable {

    private final String sourceKey;

    private final String name;
    private final String address;
    private final int port;

    private String[] tags;

    public Registration(String sourceKey, String name, String address, int port, String...tags) {
        if (sourceKey == null) {
            throw new IllegalArgumentException("Source key name cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Registration name cannot be null");
        }
        if ( address == null ) {
            throw new IllegalArgumentException("Address cannot be null" );
        }
        this.sourceKey = sourceKey;
        this.name = name;
        this.address = address;
        this.port = port;
        this.tags = tags;
        if ( this.tags == null ) {
            this.tags = new String[]{};
        }
    }

    public String getSourceKey() {
        return this.sourceKey;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        return this.address + ":" + this.port;
    }

    @Override
    public int hashCode() {
        return this.sourceKey.hashCode() + this.name.hashCode() + this.address.hashCode() + this.port;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Registration)) {
            return false;
        }

        Registration that = (Registration) obj;

        return (this.sourceKey.equals(that.sourceKey) && this.name.equals(that.name) && this.address.equals(that.address) && this.port == that.port);
    }

    public String[] getTags() {
        return tags;
    }

    public boolean hasTag(String tag) {
        for (String s : this.tags) {
            if ( s.equals( tag ) ) {
                return true;
            }
        }

        return false;
    }
}
