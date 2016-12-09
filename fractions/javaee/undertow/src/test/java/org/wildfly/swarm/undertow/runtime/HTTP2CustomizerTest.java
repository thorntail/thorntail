package org.wildfly.swarm.undertow.runtime;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.server.HttpsListener;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.UndertowProperties;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class HTTP2CustomizerTest {

    @Test
    public void testHTTP2Enabled() {

        HTTP2Customizer customizer = new HTTP2Customizer();
        customizer.undertow = new UndertowFraction().applyDefaults();
        Server server = customizer.undertow.subresources().server(UndertowProperties.DEFAULT_SERVER);

        AtomicReference<HttpsListener> listener = new AtomicReference<>();
        server.httpsListener("default-https", (config) -> {
            listener.set(config);
        });

        assertThat(listener.get()).isNotNull();

        assertThat(listener.get().enableHttp2()).isNull();
        customizer.customize();
        assertThat(listener.get().enableHttp2()).isTrue();

    }
}
