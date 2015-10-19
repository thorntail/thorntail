package org.wildfly.swarm.undertow;

import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.undertow.BufferCache;
import org.wildfly.swarm.config.undertow.HandlerConfiguration;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.ServletContainer;
import org.wildfly.swarm.config.undertow.server.Host;
import org.wildfly.swarm.config.undertow.server.HttpListener;
import org.wildfly.swarm.config.undertow.servlet_container.JspSetting;
import org.wildfly.swarm.config.undertow.servlet_container.WebsocketsSetting;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.SocketBinding;

/**
 * @author Bob McWhirter
 */
public class UndertowFraction extends Undertow<UndertowFraction> implements Fraction {

    public UndertowFraction() {
    }

    public static UndertowFraction createDefaultFraction() {
        UndertowFraction fraction = new UndertowFraction();

        fraction.server(
                new Server("default-server")
                        .httpListener(new HttpListener("default")
                                .socketBinding("http"))
                        .host(new Host("default-host")))
                .bufferCache(new BufferCache("default"))
                .servletContainer(new ServletContainer("default")
                        .websocketsSetting(new WebsocketsSetting())
                        .jspSetting(new JspSetting()))
                .handlerConfiguration(new HandlerConfiguration());

        return fraction;
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        initContext.socketBinding(
                new SocketBinding("http")
                        .port("${jboss.http.port:8080}"));
        initContext.socketBinding(
                new SocketBinding("https")
                        .port("${jboss.https.port:8443}"));
    }
}
