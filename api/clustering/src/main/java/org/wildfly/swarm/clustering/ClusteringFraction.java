package org.wildfly.swarm.clustering;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.SocketBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class ClusteringFraction implements Fraction {

    private List<Channel> channels = new ArrayList<Channel>();
    private List<Stack> stacks = new ArrayList<Stack>();

    private String defaultChannel;
    private String defaultStack;

    public ClusteringFraction() {
    }

    public ClusteringFraction channel(Channel channel) {
        this.channels.add( channel );
        return this;
    }

    public ClusteringFraction defaultChannel(Channel channel) {
        this.channels.add( channel );
        return defaultChannel( channel.name() );
    }

    public ClusteringFraction defaultChannel(String channel) {
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

    public ClusteringFraction stack(Stack stack) {
        this.stacks.add( stack );
        return this;
    }

    public ClusteringFraction defaultStack(Stack stack) {
        this.stacks.add( stack );
        return defaultStack( stack.name() );
    }

    public ClusteringFraction defaultStack(String stack) {
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
