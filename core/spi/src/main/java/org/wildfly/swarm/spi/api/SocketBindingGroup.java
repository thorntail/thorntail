/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.spi.api;

import java.util.ArrayList;
import java.util.List;


/**
 * A group of inbound and outbound socket-bindings.
 *
 * <p>The default socket-binding group is typically named {@code standard-sockets}.</p>
 *
 * <p>All bindings within a group may be universally offset to accommodate shifting
 * an entire server up or down some set number of ports.</p>
 *
 * @author Bob McWhirter
 * @see SocketBinding
 * @see OutboundSocketBinding
 */
public class SocketBindingGroup {

    private final String name;

    private String defaultInterace;

    private String portOffsetExpression;

    private List<SocketBinding> socketBindings = new ArrayList<>();

    private List<OutboundSocketBinding> outboundSocketBindings = new ArrayList<>();

    /**
     * Create a new socket-binding group.
     *
     * @param name                 The name of the group.
     * @param defaultInterface     The name of the interface to bind to.
     * @param portOffsetExpression The port offset expression.
     */
    public SocketBindingGroup(String name, String defaultInterface, String portOffsetExpression) {
        this.name = name;
        this.defaultInterace = defaultInterface;
        this.portOffsetExpression = portOffsetExpression;
    }

    /**
     * Retrieve the name of this group.
     *
     * @return The name of this group.
     */
    public String name() {
        return this.name;
    }

    /**
     * Retrieve the name of the default interface.
     *
     * @return The name of the default interface.
     */
    public String defaultInterface() {
        return this.defaultInterace;
    }

    public SocketBindingGroup defaultInterface(String defaultInterface) {
        this.defaultInterace = defaultInterface;
        return this;
    }

    /**
     * Retrieve the port-offset expression.
     *
     * @return The port offset expression.
     */
    public String portOffsetExpression() {
        return this.portOffsetExpression;
    }

    public SocketBindingGroup portOffset(String expr) {
        this.portOffsetExpression = expr;
        return this;
    }

    public SocketBindingGroup portOffset(int offset) {
        this.portOffsetExpression = "" + offset;
        return this;
    }

    /**
     * Add a socket-binding to this group.
     *
     * @param binding The binding to add.
     * @return this group.
     */
    public SocketBindingGroup socketBinding(SocketBinding binding) {
        this.socketBindings.add(binding);
        return this;
    }

    /**
     * Retrieve a socket-binding by name.
     *
     * @param name The socket-binding name.
     * @return The socket-binding if present, otherwise {@code null}.
     */
    public SocketBinding socketBinding(String name) {
        return this.socketBindings.stream().filter(e -> e.name().equals(name)).findFirst().orElse(null);
    }

    /**
     * Retrieve all socket-bindings attached to this group.
     *
     * @return All socket-bindings attached to this group.
     */
    public List<SocketBinding> socketBindings() {
        return this.socketBindings;
    }

    /**
     * Add an outbound socket-binding to this group.
     *
     * @param binding The binding to add.
     * @return this group.
     */
    public SocketBindingGroup outboundSocketBinding(OutboundSocketBinding binding) {
        this.outboundSocketBindings.add(binding);
        return this;
    }

    /**
     * Retrieve all outbound socket-bindings attached to this group.
     *
     * @return All outbound socket-bindings attached to this group.
     */
    public List<OutboundSocketBinding> outboundSocketBindings() {
        return this.outboundSocketBindings;
    }
}

