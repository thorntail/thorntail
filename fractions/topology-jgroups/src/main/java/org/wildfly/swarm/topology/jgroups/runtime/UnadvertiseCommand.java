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
package org.wildfly.swarm.topology.jgroups.runtime;

import org.wildfly.clustering.dispatcher.Command;
import org.wildfly.swarm.topology.jgroups.runtime.JGroupsTopologyConnector;
import org.wildfly.swarm.topology.runtime.Registration;

/**
 * @author Bob McWhirter
 */
public class UnadvertiseCommand implements Command<Void, JGroupsTopologyConnector> {

    public UnadvertiseCommand(Registration registration) {
        this.registration = registration;
    }

    @Override
    public Void execute(JGroupsTopologyConnector context) throws Exception {
        context.unregister(this.registration);
        return null;
    }

    private final Registration registration;
}
