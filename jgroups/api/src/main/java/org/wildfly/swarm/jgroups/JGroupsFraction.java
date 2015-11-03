/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.jgroups;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.SocketBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class JGroupsFraction implements Fraction {

    private List<Channel> channels = new ArrayList<Channel>();
    private List<Stack> stacks = new ArrayList<Stack>();

    private String defaultChannel;
    private String defaultStack;

    public JGroupsFraction() {
    }

    public JGroupsFraction channel(Channel channel) {
        this.channels.add( channel );
        return this;
    }

    public JGroupsFraction defaultChannel(Channel channel) {
        this.channels.add( channel );
        return defaultChannel( channel.name() );
    }

    public JGroupsFraction defaultChannel(String channel) {
        this.defaultChannel = channel;
        return this;
    }

    public Channel defaultChannel() {
        return this.channels.stream()
                .filter( (e)-> e.name().equals( this.defaultChannel ) )
                .findFirst().orElse(null);
    }

    public List<Channel> channels() {
        return this.channels;
    }

    public JGroupsFraction stack(Stack stack) {
        this.stacks.add( stack );
        return this;
    }

    public JGroupsFraction defaultStack(Stack stack) {
        this.stacks.add( stack );
        return defaultStack( stack.name() );
    }

    public JGroupsFraction defaultStack(String stack) {
        this.defaultStack = stack;
        return this;
    }

    public List<Stack> stacks() {
        return this.stacks;
    }

    public Stack defaultStack() {
        return this.stacks.stream()
                .filter( (e)->e.name().equals( this.defaultStack ) )
                .findFirst().orElse(null);
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        initContext.socketBinding(
                new SocketBinding("jgroups-udp")
                        .port(55200)
                        .multicastAddress("${jboss.default.multicast.address:230.0.0.4}")
                        .multicastPort(45688));

        initContext.socketBinding(
                new SocketBinding("jgroups-udp-fd")
                        .port(54200));

        initContext.socketBinding(
                new SocketBinding("jgroups-mping")
                        .port(0)
                        .multicastAddress("${jboss.default.multicast.address:230.0.0.4}")
                        .multicastPort(45700));

        initContext.socketBinding(
                new SocketBinding("jgroups-tcp")
                        .port(7600));

        initContext.socketBinding(
                new SocketBinding("jgroups-tcp-fd")
                        .port(57600));

    }
}
