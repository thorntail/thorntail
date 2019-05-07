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
package org.wildfly.swarm.neo4j;

import org.wildfly.swarm.config.Neo4jdriver;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Scott Marlow
 */

@WildFlyExtension(module = "org.wildfly.extension.nosql.neo4j")
@MarshalDMR
@Configurable("thorntail.neo4jdriver")
public class Neo4jFraction extends Neo4jdriver<Neo4jFraction> implements Fraction<Neo4jFraction> {
    public static Neo4jFraction createDefaultFraction() {
        return new Neo4jFraction().applyDefaults();
    }

    public Neo4jFraction applyDefaults() {
        // set defaults

        return this;
    }


}
