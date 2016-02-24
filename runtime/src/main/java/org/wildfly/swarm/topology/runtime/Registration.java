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
package org.wildfly.swarm.topology.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.wildfly.swarm.topology.Topology;

/**
 * @author Bob McWhirter
 */
public class Registration implements Topology.Entry, Serializable {

    private final String sourceKey;

    private final String name;
    private final String address;
    private final int port;

    private List<String> tags = new ArrayList<>();

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
        this.tags.addAll( Arrays.asList( tags ) );
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

    public Registration addTags(List<String> tags) {
        this.tags.addAll( tags );
        return this;
    }

    public String toString() {
        return "[Registration: " + this.name + "; " + this.address + ":" + this.port + "; " + Arrays.asList( this.tags ) + "]";
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

    public List<String> getTags() {
        return Collections.unmodifiableList( this.tags );
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
