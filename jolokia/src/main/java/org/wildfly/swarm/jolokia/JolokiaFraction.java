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
package org.wildfly.swarm.jolokia;

import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.DefaultFraction;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Bob McWhirter
 */
@Singleton
@DefaultFraction
public class JolokiaFraction implements Fraction {

    public JolokiaFraction() {
        this("jolokia");
    }

    public JolokiaFraction(String context) {
        this.context = context;
    }

    public JolokiaFraction context(String context) {
        this.context = context;
        return this;
    }

    public String context() {
        return this.context;
    }

    private String context;
}
