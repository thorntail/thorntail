package io.thorntail.testsuite.jms.opentracing.driven;

import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.thorntail.test.EphemeralPorts;
import io.thorntail.test.ThorntailTestRunner;
import io.thorntail.testutils.opentracing.SpanNode;
import io.thorntail.testutils.opentracing.SpanTree;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;
import static io.thorntail.testutils.opentracing.SpanNode.assertThat;
import static io.thorntail.testutils.opentracing.SpanTree.assertThat;

@RunWith(ThorntailTestRunner.class)
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
