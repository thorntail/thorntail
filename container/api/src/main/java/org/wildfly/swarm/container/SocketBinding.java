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

import org.wildfly.swarm.container.internal.BaseSocketBinding;

/**
 * @author Bob McWhirter
 */
public class SocketBinding extends BaseSocketBinding {

    public SocketBinding(String name) {
        super(name);
    }

    public SocketBinding port(int port) {
        this.portExpression = "" + port;
        return this;
    }

    public SocketBinding port(String portExpression) {
        this.portExpression = portExpression;
        return this;
    }

    public String portExpression() {
        return this.portExpression;
    }

    public SocketBinding multicastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
        return this;
    }

    public String multicastAddress() {
        return this.multicastAddress;
    }

    public SocketBinding multicastPort(int port) {
        this.multicastPortExpression = "" + port;
        return this;
    }

    public SocketBinding multicastPort(String port) {
        this.multicastPortExpression = port;
        return this;
    }

    public String multicastPortExpression() {
        return this.multicastPortExpression;
    }

    private String portExpression;

    private String multicastAddress;

    private String multicastPortExpression;


}
