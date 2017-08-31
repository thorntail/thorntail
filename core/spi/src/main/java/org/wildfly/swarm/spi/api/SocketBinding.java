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

/**
 * An inbound socket-binding.
 *
 * <p>Defines a named inbound normal or multicast socket-binding.</p>
 *
 * <p>Inbound socket-bindings are used to define open ports for functionality
 * such as HTTP listeners, JGroups multicast groups, etc.</p>
 *
 * @author Bob McWhirter
 * @see SocketBindingGroup
 */
public class SocketBinding {

    private String name;

    private String iface;

    private String portExpression;

    private String multicastAddress;

    private String multicastPortExpression;

    /**
     * Construct a new socket-binding.
     *
     * @param name The name of the binding.
     */
    public SocketBinding(String name) {
        this.name = name;
    }

    /**
     * Retrieve the name of the binding.
     *
     * @return the name of the binding.
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the interface for this binding.
     *
     * @param iface The name of the interface.
     * @return This binding.
     */
    public SocketBinding iface(String iface) {
        this.iface = iface;
        return this;
    }

    /**
     * Retrieve the interface for this binding.
     *
     * @return The name of the interface.
     */
    public String iface() {
        return this.iface;
    }

    /**
     * Set the port.
     *
     * @param port The port.
     * @return this binding.
     */
    public SocketBinding port(int port) {
        this.portExpression = "" + port;
        return this;
    }

    /**
     * Set the port expression
     *
     * @param portExpression The port expression.
     * @return this binding.
     */
    public SocketBinding port(String portExpression) {
        this.portExpression = portExpression;
        return this;
    }

    /**
     * Retrieve the port expression.
     *
     * @return The port expression.
     */
    public String portExpression() {
        return this.portExpression;
    }

    /**
     * Set the multicast address or expression.
     *
     * @param multicastAddress The multicast address or expression.
     * @return this binding.
     */
    public SocketBinding multicastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
        return this;
    }

    /**
     * Retrieve the multicast address or expression.
     *
     * @return The multicast address or expression.
     */
    public String multicastAddress() {
        return this.multicastAddress;
    }

    /**
     * Set the multicast port.
     *
     * @param port The multicast port.
     * @return this binding.
     */
    public SocketBinding multicastPort(int port) {
        this.multicastPortExpression = "" + port;
        return this;
    }

    /**
     * Set the multicast port expression.
     *
     * @param port The multicast port expression.
     * @return this binding.
     */
    public SocketBinding multicastPort(String port) {
        this.multicastPortExpression = port;
        return this;
    }

    /**
     * Retrieve the multicast port expression.
     *
     * @return The multicast port expression.
     */
    public String multicastPortExpression() {
        return this.multicastPortExpression;
    }


}
