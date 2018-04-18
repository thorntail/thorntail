package io.thorntail.testsuite.jms.basic;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.jms.JMSContext;

import io.thorntail.test.EphemeralPorts;
import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
@EphemeralPorts
public class JMSAppIT {

    @Test
    public void test() {
        this.sender.send( "one" );
        this.sender.send( "two" );
        this.sender.send( "three" );

        Set<String> received = new HashSet<>();

        received.add( this.receiver.receive() );
        received.add( this.receiver.receive() );
        received.add( this.receiver.receive() );

        assertThat(received).hasSize(3);
        assertThat(received).contains("one");
        assertThat(received).contains("two");
        assertThat(received).contains("three");
    }

    @Inject
    Sender sender;

    @Inject
    Receiver receiver;

    @Inject
    private JMSContext context;

}
