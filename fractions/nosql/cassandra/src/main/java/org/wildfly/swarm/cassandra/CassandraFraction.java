/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.cassandra;

import org.wildfly.swarm.config.Cassandradriver;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Scott Marlow
 */

@WildFlyExtension(module = "org.wildfly.extension.nosql.cassandra")
@MarshalDMR
@Configurable("thorntail.cassandradriver")
public class CassandraFraction extends Cassandradriver<CassandraFraction> implements Fraction<CassandraFraction> {
    public static CassandraFraction createDefaultFraction() {
        return new CassandraFraction().applyDefaults();
    }

    public CassandraFraction applyDefaults() {
        // set Cassandra defaults

        return this;
    }


}
