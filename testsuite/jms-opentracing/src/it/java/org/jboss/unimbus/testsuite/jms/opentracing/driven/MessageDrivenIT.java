package org.jboss.unimbus.testsuite.jms.opentracing.driven;

import java.util.List;

import javax.inject.Inject;
import javax.jms.JMSContext;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import org.jboss.unimbus.test.EphemeralPorts;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.jboss.unimbus.testsuite.jms.opentracing.util.SpanNode;
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

        assertThat(this.results.get()).hasSize(3);
        assertThat(this.results.get()).contains("one");
        assertThat(this.results.get()).contains("two");
        assertThat(this.results.get()).contains("three");

        SpanTree tree = new SpanTree((MockTracer) this.tracer);
        SpanNode root = tree.getRootNodes().get(0);
        assertThat(root.getChildren()).hasSize(3);

        for (SpanNode spanNode : root.getChildren()) {
            assertThat(spanNode.getTags()).hasSize(2);
            assertThat(spanNode.operationName()).isEqualTo("send");
            assertThat(spanNode.getTags().get(Tags.MESSAGE_BUS_DESTINATION.getKey())).isEqualTo("driven");
            assertThat(spanNode.getTags().get(Tags.SPAN_KIND.getKey())).isEqualTo(Tags.SPAN_KIND_PRODUCER);
            assertThat(spanNode.getChildren()).hasSize(1);

            SpanNode child = spanNode.getChildren().get(0);
            assertThat(child.getTags()).hasSize(3);
            assertThat(child.operationName()).isEqualTo("receive");
            assertThat(child.getTags().get(Tags.MESSAGE_BUS_DESTINATION.getKey())).isEqualTo("driven");
            assertThat(child.getTags().get(Tags.SPAN_KIND.getKey())).isEqualTo(Tags.SPAN_KIND_CONSUMER);
            assertThat(child.getTags().get("jms.message.id")).isNotNull();
        }
    }

    @Inject
    Sender sender;

    @Inject
    Results results;

    @Inject
    Tracer tracer;

}
