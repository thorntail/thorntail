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

import org.wildfly.swarm.config.Modcluster;
import org.wildfly.swarm.config.modcluster.ConfigurationModClusterConfig;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.annotations.Default;
import org.wildfly.swarm.spi.api.annotations.ExtensionModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;

/**
 * @author Stuart Douglas
 */
@ExtensionModule("org.wildfly.extension.mod_cluster")
@MarshalDMR
public class ModclusterFraction extends Modcluster<ModclusterFraction> implements Fraction {

    public ModclusterFraction() {
    }

    @Default
    public static ModclusterFraction createDefaultFraction() {
        ModclusterFraction fraction = new ModclusterFraction();
        fraction.configurationModClusterConfig(new ConfigurationModClusterConfig()
                .advertiseSocket("modcluster")
                .advertise(true)
                .connector("default"));
        return fraction;
    }
    @Override
    public void postInitialize(Fraction.PostInitContext initContext) {
        //TODO: this should probably not be hard coded
        initContext.socketBinding(
                new SocketBinding("modcluster")
                        .port(0)
                        .multicastAddress("224.0.1.105")
                        .multicastPort("23364"));
    }

}
