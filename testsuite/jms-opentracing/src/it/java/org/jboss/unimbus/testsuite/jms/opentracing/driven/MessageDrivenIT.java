package org.jboss.unimbus.testsuite.jms.opentracing.driven;

import java.util.List;

import javax.inject.Inject;
import javax.jms.JMSContext;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import org.jboss.unimbus.test.EphemeralPorts;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.jboss.unimbus.testsuite.jms.opentracing.util.SpanTree;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(UNimbusTestRunner.class)
@EphemeralPorts
public class MessageDrivenIT {

    @Test
    public void test() throws InterruptedException {

        ActiveSpan span = this.tracer.buildSpan("root").startActive();
        this.sender.send("one");
        this.sender.send("two");
        this.sender.send("three");
        span.deactivate();

        this.results.await(3, 5000);

        SpanTree tree = new SpanTree((MockTracer) this.tracer);
        System.err.println( tree );

        assertThat(this.results.get()).hasSize(3);
        assertThat(this.results.get()).contains("one");
        assertThat(this.results.get()).contains("two");
        assertThat(this.results.get()).contains("three");

        List<MockSpan> spans = ((MockTracer) this.tracer).finishedSpans();
    }

    @Inject
    Sender sender;

    @Inject
    Results results;

    @Inject
    private JMSContext context;

    @Inject
    Tracer tracer;

}
