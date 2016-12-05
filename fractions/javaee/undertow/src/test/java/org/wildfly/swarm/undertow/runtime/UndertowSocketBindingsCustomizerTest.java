package org.wildfly.swarm.undertow.runtime;

import org.junit.Test;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.undertow.UndertowFraction;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class UndertowSocketBindingsCustomizerTest {

    @Test
    public void testDefaultPorts() {
        UndertowSocketBindingsCustomizer customizer = new UndertowSocketBindingsCustomizer();

        customizer.fraction = new UndertowFraction();
        customizer.group =  new SocketBindingGroup("standard-sockets", "public", "0");
        customizer.customize();

        assertThat( customizer.group.socketBindings() ).hasSize(2);

        SocketBinding http = customizer.group.socketBinding("http");

        assertThat( http ).isNotNull();
        assertThat( http.portExpression() ).isEqualTo( "8080" );

        SocketBinding https = customizer.group.socketBinding("https");

        assertThat( https ).isNotNull();
        assertThat( https.portExpression() ).isEqualTo( "8443" );
    }

    @Test
    public void testExplicitHttpPort() {
        UndertowSocketBindingsCustomizer customizer = new UndertowSocketBindingsCustomizer();

        customizer.fraction = new UndertowFraction();
        customizer.fraction.httpPort(8675);

        customizer.group =  new SocketBindingGroup("standard-sockets", "public", "0");

        customizer.customize();

        assertThat( customizer.group.socketBindings() ).hasSize(2);

        SocketBinding http = customizer.group.socketBinding("http");

        assertThat( http ).isNotNull();
        assertThat( http.portExpression() ).isEqualTo( "8675" );

        SocketBinding https = customizer.group.socketBinding("https");

        assertThat( https ).isNotNull();
        assertThat( https.portExpression() ).isEqualTo( "8443" );
    }

    @Test
    public void testExplicitHttpsPort() {
        UndertowSocketBindingsCustomizer customizer = new UndertowSocketBindingsCustomizer();

        customizer.fraction = new UndertowFraction();
        customizer.fraction.httpsPort(8675);

        customizer.group =  new SocketBindingGroup("standard-sockets", "public", "0");

        customizer.customize();

        assertThat( customizer.group.socketBindings() ).hasSize(2);

        SocketBinding http = customizer.group.socketBinding("http");

        assertThat( http ).isNotNull();
        assertThat( http.portExpression() ).isEqualTo( "8080" );

        SocketBinding https = customizer.group.socketBinding("https");

        assertThat( https ).isNotNull();
        assertThat( https.portExpression() ).isEqualTo( "8675" );
    }
}
