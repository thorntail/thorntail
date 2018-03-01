package org.jboss.unimbus.testsuite.jms.opentracing.driven;

import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import org.jboss.unimbus.test.EphemeralPorts;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.jboss.unimbus.testutils.opentracing.SpanNode;
import org.jboss.unimbus.testutils.opentracing.SpanTree;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;
import static org.jboss.unimbus.testutils.opentracing.SpanNode.assertThat;
import static org.jboss.unimbus.testutils.opentracing.SpanTree.assertThat;

@RunWith(UNimbusTestRunner.class)
@EphemeralPorts
public class MessageDrivenIT {

    @Test
    public void test() throws InterruptedException, TimeoutException {

        ActiveSpan span = this.tracer.buildSpan("root").startActive();
        this.sender.send("one");
        this.sender.send("two");
        this.sender.send("three");
        span.deactivate();

        this.results.await(3, 5000);

        assertThat(this.results.get()).hasSize(3);
        assertThat(this.results.get()).contains("one");
        assertThat(this.results.get()).contains("two");
        assertThat(this.results.get()).contains("three");

        SpanTree tree = new SpanTree((MockTracer) this.tracer);
        System.err.println("---tree---");
        System.err.println(tree);
        System.err.println("---tree---");

        assertThat(tree).hasRootSpans(1);
        SpanNode root = tree.getRootNodes().get(0);
        assertThat(root).hasChildSpans(3);

        for (SpanNode send : root.getChildren()) {
            assertThat(send).hasChildSpans(1);
            assertThat(send).hasOperationName("jms-send");
            assertThat(send).hasTag("span.kind", "producer");
            assertThat(send).hasTag("message_bus.destination", "driven");
            SpanNode receive = send.getChildren().get(0);
            assertThat(receive).hasNoChildSpans();
            assertThat(receive).hasOperationName("jms-receive");
            assertThat(receive).hasTag("span.kind", "consumer");
            assertThat(receive).hasTag("message_bus.destination", "driven");
            assertThat(receive).hasTag("jms.message.id");
        }
    }

    @Inject
    Sender sender;

    @Inject
    Results results;

    @Inject
    Tracer tracer;

}
