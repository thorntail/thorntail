package io.thorntail.testsuite.jms.driven;

import javax.inject.Inject;
import javax.jms.JMSContext;

import io.thorntail.test.EphemeralPorts;
import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
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
