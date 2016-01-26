package org.wildfly.swarm.topology.runtime;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.wildfly.swarm.topology.Topology;

/**
 * @author Bob McWhirter
 */
public class Registration implements Serializable {

    private final String sourceKey;
    private final String name;

    private final Set<EndPoint> endPoints = new HashSet<>();

    public Registration(String sourceKey, String name) {
        if (sourceKey == null) {
            throw new IllegalArgumentException("Source key name cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Registration name cannot be null");
        }
        this.sourceKey = sourceKey;
        this.name = name;
    }

    public String toString() {
        return "[Registration: " + this.sourceKey + ":" + this.name + ": " + this.endPoints + "]";
    }

    public Registration endPoint(EndPoint endPoint) {
        this.endPoints.add(endPoint);
        return this;
    }

    public String getSourceKey() {
        return this.sourceKey;
    }

    public String getName() {
        return this.name;
    }

    public Set<EndPoint> endPoints() {
        return Collections.unmodifiableSet(this.endPoints);
    }

    public Set<EndPoint> endPoints(EndPoint.Visibility visibility) {
        return Collections.unmodifiableSet(
                this.endPoints.stream().filter(e -> e.getVisibility().equals(visibility))
                        .collect(Collectors.toSet()));
    }

    @Override
    public int hashCode() {
        return this.sourceKey.hashCode() + this.name.hashCode() + this.endPoints.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Registration)) {
            return false;
        }

        Registration that = (Registration) obj;

        return (this.sourceKey.equals(that.sourceKey) && this.name.equals(that.name) && this.endPoints.equals(that.endPoints));
    }

    public static class EndPoint implements Topology.Entry, Serializable {

        private final String address;

        private final int port;

        private Visibility visibility;

        public EndPoint(String address, int port) {
            this.address = address;
            this.port = port;
            this.visibility = Visibility.PUBLIC;
        }

        public String getAddress() {
            return this.address;
        }

        public int getPort() {
            return this.port;
        }

        public Visibility getVisibility() {
            return this.visibility;
        }

        public EndPoint isPublic() {
            this.visibility = Visibility.PUBLIC;
            return this;
        }

        public EndPoint isPrivate() {
            this.visibility = Visibility.PRIVATE;
            return this;
        }

        @Override
        public String toString() {
            return this.address +":" + this.port;
        }

        @Override
        public int hashCode() {
            return (address + port + visibility).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EndPoint)) {
                return false;
            }

            EndPoint that = (EndPoint) obj;

            return (this.address.equals(that.address) && this.port == that.port && this.visibility == that.visibility);
        }

        public enum Visibility {
            PUBLIC,
            PRIVATE
        }
    }

}
