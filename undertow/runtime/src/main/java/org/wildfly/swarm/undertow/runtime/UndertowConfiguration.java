package org.wildfly.swarm.undertow.runtime;

import org.jboss.dmr.ModelNode;
import org.wildfly.config.invocation.Marshaller;
import org.wildfly.swarm.config.undertow.subsystem.bufferCache.BufferCache;
import org.wildfly.swarm.config.undertow.subsystem.configuration.Handler;
import org.wildfly.swarm.config.undertow.subsystem.server.Server;
import org.wildfly.swarm.config.undertow.subsystem.server.host.Host;
import org.wildfly.swarm.config.undertow.subsystem.server.httpListener.HttpListener;
import org.wildfly.swarm.config.undertow.subsystem.servletContainer.ServletContainer;
import org.wildfly.swarm.config.undertow.subsystem.servletContainer.setting.Jsp;
import org.wildfly.swarm.config.undertow.subsystem.servletContainer.setting.Websockets;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.undertow.UndertowFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class UndertowConfiguration extends AbstractServerConfiguration<UndertowFraction> {

    public UndertowConfiguration() {
        super(UndertowFraction.class);
    }

    @Override
    public UndertowFraction defaultFraction() {

        UndertowFraction fraction = new UndertowFraction();

        fraction.server(new Server("default-server")
                        .httpListener(new HttpListener("default").socketBinding("http"))
                        .host(new Host("default-host")))

                .bufferCache(new BufferCache("default"))

                .servletContainer(new ServletContainer("default")
                        .websockets(new Websockets())
                        .jsp(new Jsp()))

                .handler(new Handler());

        return fraction;
    }

    @Override
    public List<ModelNode> getList(UndertowFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.undertow");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Unable to configure Undertow subsystem. " + e);
        }

        return list;

    }
}
