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

import java.util.ArrayList;
import java.util.List;


/**
 * @author Bob McWhirter
 */
public class SocketBindingGroup {


    public SocketBindingGroup(String name, String defaultInterface, String portOffsetExpression) {
        this.name = name;
        this.defaultInterace = defaultInterface;
        this.portOffsetExpression = portOffsetExpression;
    }

    public String name() {
        return this.name;
    }

    public String defaultInterface() {
        return this.defaultInterace;
    }

    public String portOffsetExpression() {
        return this.portOffsetExpression;
    }

    public SocketBindingGroup socketBinding(SocketBinding binding) {
        this.socketBindings.add(binding);
        return this;
    }

    public List<SocketBinding> socketBindings() {
        return this.socketBindings;
    }

    public SocketBindingGroup outboundSocketBinding(OutboundSocketBinding binding) {
        this.outboundSocketBindings.add(binding);
        return this;
    }

    public List<OutboundSocketBinding> outboundSocketBindings() {
        return this.outboundSocketBindings;
    }

    private final String name;

    private final String defaultInterace;

    private final String portOffsetExpression;

    private List<SocketBinding> socketBindings = new ArrayList<>();

    private List<OutboundSocketBinding> outboundSocketBindings = new ArrayList<>();
}

