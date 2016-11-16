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
package org.wildfly.swarm.mod_cluster;

import javax.annotation.PostConstruct;

import org.wildfly.swarm.config.Modcluster;
import org.wildfly.swarm.config.modcluster.ConfigurationModClusterConfig;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.spi.api.Defaultable.string;

/**
 * @author Stuart Douglas
 */
@WildFlyExtension(module = "org.wildfly.extension.mod_cluster")
@MarshalDMR
public class ModclusterFraction extends Modcluster<ModclusterFraction> implements Fraction<ModclusterFraction> {

    public ModclusterFraction() {
    }

    public static ModclusterFraction createDefaultFraction() {
        return new ModclusterFraction().applyDefaults();
    }

    @PostConstruct
    public void postConstruct() {
        applyDefaults();
    }

    public ModclusterFraction applyDefaults() {
        return configurationModClusterConfig(new ConfigurationModClusterConfig()
                .advertiseSocket("modcluster")
                .advertise(true)
                .connector("default"));
    }

    public ModclusterFraction multicastAddress(String address) {
        this.multicastAddress.set( address );
        return this;
    }

    public String multicastAddress() {
        return this.multicastAddress.get();
    }

    public ModclusterFraction multicastPort(int port) {
        this.multicastPort.set( port );
        return this;
    }

    public int multicastPort() {
        return this.multicastPort.get();
    }

    private Defaultable<String> multicastAddress = string("224.01.105");
    private Defaultable<Integer> multicastPort = integer(23364);

    //private Defaultable<String> multicastAddress = string("swarm.modcluster.multicast.address", "224.01.105");
    //private Defaultable<Integer> multicastPort = integer("swarm.modcluster.multicast.port", 23364);


}
