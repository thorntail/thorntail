package org.wildfly.swarm.undertow;

import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.SocketBinding;

/**
 * @author Bob McWhirter
 */
public class UndertowFraction extends Undertow<UndertowFraction> implements Fraction {

    public UndertowFraction() {
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
