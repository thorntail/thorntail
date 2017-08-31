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
package org.wildfly.swarm.internal;

import org.wildfly.swarm.spi.api.OutboundSocketBinding;

/**
 * @author Bob McWhirter
 */
public class OutboundSocketBindingRequest {

    public OutboundSocketBindingRequest(String socketBindingGroup, OutboundSocketBinding binding) {
        this.socketBindingGroup = socketBindingGroup;
        this.binding = binding;
    }

    public String socketBindingGroup() {
        return this.socketBindingGroup;
    }

    public OutboundSocketBinding outboundSocketBinding() {
        return this.binding;
    }

    private final String socketBindingGroup;
    private final OutboundSocketBinding binding;
}
