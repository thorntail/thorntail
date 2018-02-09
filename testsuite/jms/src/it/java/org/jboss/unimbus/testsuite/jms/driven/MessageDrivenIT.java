package org.jboss.unimbus.testsuite.jms.driven;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.jms.JMSContext;

import org.jboss.unimbus.jms.MessageDriven;
import org.jboss.unimbus.test.EphemeralPorts;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(UNimbusTestRunner.class)
@EphemeralPorts
public class MessageDrivenIT {

    @Test
    public void test() throws InterruptedException {
        this.sender.send("one");
        this.sender.send("two");
        this.sender.send("three");

        Thread.sleep(1000);

        assertThat( this.results.get()).hasSize(3);
        assertThat( this.results.get()).contains("one");
        assertThat( this.results.get()).contains("two");
        assertThat( this.results.get()).contains("three");

    }

    @Inject
    Sender sender;

    @Inject
    Results results;

    @Inject
    private JMSContext context;

}
