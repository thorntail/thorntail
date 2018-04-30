package io.thorntail.testsuite.websockets;

import java.net.URI;

import javax.inject.Inject;

import io.thorntail.test.ThorntailTestRunner;
import org.fest.assertions.Assertions;
import io.thorntail.servlet.annotation.Primary;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 4/6/18.
 */
@RunWith(ThorntailTestRunner.class)
public class WebSocketsAppTest {

    @Test
    public void testIt() throws Exception {
        Client client = new Client(this.server);

        client.send("hi");
        client.send("there");
        client.send("bob");

        client.await(3);

        client.close();

        Assertions.assertThat(client.getMessages()).hasSize(3);

        Assertions.assertThat(client.getMessages()).contains("HI");
        Assertions.assertThat(client.getMessages()).contains("THERE");
        Assertions.assertThat(client.getMessages()).contains("BOB");

    }

    @Inject
    @Primary
    URI server;


}
