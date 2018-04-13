package org.jboss.unimbus.testsuite.websockets;

import java.net.URI;

import javax.inject.Inject;

import org.jboss.unimbus.servlet.annotation.Primary;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 4/6/18.
 */
@RunWith(UNimbusTestRunner.class)
public class WebSocketsAppTest {

    @Test
    public void testIt() throws Exception {
        Client client = new Client(this.server);

        client.send("hi");
        client.send("there");
        client.send("bob");

        client.await(3);

        client.close();

        assertThat(client.getMessages()).hasSize(3);

        assertThat(client.getMessages()).contains("HI");
        assertThat(client.getMessages()).contains("THERE");
        assertThat(client.getMessages()).contains("BOB");

    }

    @Inject
    @Primary
    URI server;


}
