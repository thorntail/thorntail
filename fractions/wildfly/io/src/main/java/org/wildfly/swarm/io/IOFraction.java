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
package org.wildfly.swarm.io;

import org.wildfly.swarm.config.IO;
import org.wildfly.swarm.config.io.BufferPool;
import org.wildfly.swarm.config.io.Worker;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 */
@WildFlyExtension(module = "org.wildfly.extension.io")
@MarshalDMR
public class IOFraction extends IO<IOFraction> implements Fraction<IOFraction> {

    public IOFraction applyDefaults() {
        return worker(new Worker("default"))
                .bufferPool(new BufferPool("default"));
    }
}
