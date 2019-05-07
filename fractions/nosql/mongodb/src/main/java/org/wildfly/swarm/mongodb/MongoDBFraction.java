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
package org.wildfly.swarm.mongodb;

import org.wildfly.swarm.config.Mongodb;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Scott Marlow
 */

@WildFlyExtension(module = "org.wildfly.extension.nosql.mongodb")
@MarshalDMR
@Configurable("thorntail.mongodb")
public class MongoDBFraction extends Mongodb<MongoDBFraction> implements Fraction<MongoDBFraction> {
    public static MongoDBFraction createDefaultFraction() {
        return new MongoDBFraction().applyDefaults();
    }

    public MongoDBFraction applyDefaults() {
        // set MongoDB defaults

        return this;
    }


}
