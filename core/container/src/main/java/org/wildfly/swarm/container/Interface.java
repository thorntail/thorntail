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
package org.wildfly.swarm.container;

/**
 * A network-level interface configured for the container.
 *
 * <p>An interface dictates which IP address other sockets are bound to,
 * by way of a {@link org.wildfly.swarm.spi.api.SocketBindingGroup}.</p>
 *
 * @author Bob McWhirter
 * @see org.wildfly.swarm.spi.api.SocketBindingGroup
 * @see org.wildfly.swarm.spi.api.SocketBinding
 * @see org.wildfly.swarm.spi.api.OutboundSocketBinding
 */
public class Interface {

    /**
     * Constant for use with {@link javax.inject.Named}
     */
    public static final String PUBLIC = "public-interface";

    /**
     * Constant for use with {@link javax.inject.Named}
     */
    public static final String MANAGEMENT = "management-interface";

    public Interface(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    public String getName() {
        return this.name;
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    private final String name;

    private String expression;
}
