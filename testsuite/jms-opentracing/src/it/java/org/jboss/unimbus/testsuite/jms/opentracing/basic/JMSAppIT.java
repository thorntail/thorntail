package org.jboss.unimbus.testsuite.jms.opentracing.basic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.jms.JMSContext;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import org.jboss.unimbus.test.EphemeralPorts;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.jboss.unimbus.testutils.opentracing.SpanNode;
import org.jboss.unimbus.testutils.opentracing.SpanTree;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(UNimbusTestRunner.class)
@EphemeralPorts
public class JMSAppIT {

    @Test
    public void test() {

        ActiveSpan span = this.tracer.buildSpan("root").startActive();

        this.sender.send("one");
        this.sender.send("two");
        this.sender.send("three");

        span.close();

        Set<String> received = new HashSet<>();

        received.add(this.receiver.receive());
        received.add(this.receiver.receive());
        received.add(this.receiver.receive());

        assertThat(received).hasSize(3);
        assertThat(received).contains("one");
        assertThat(received).contains("two");
        assertThat(received).contains("three");

        SpanTree tree = new SpanTree((MockTracer) this.tracer);
        assertThat(tree.getRootNodes()).hasSize(1);

        System.err.println( "--- tree ---");
        System.err.println( tree );
        System.err.println( "--- tree ---");

        SpanNode root = tree.getRootNodes().get(0);
        assertThat(root.getChildren()).hasSize(3);

        for (SpanNode spanNode : root.getChildren()) {
            assertThat(spanNode.getTags()).hasSize(2);
            assertThat(spanNode.operationName()).isEqualTo("jms-send");
            assertThat(spanNode.getTags().get(Tags.MESSAGE_BUS_DESTINATION.getKey())).isEqualTo("test");
            assertThat(spanNode.getTags().get(Tags.SPAN_KIND.getKey())).isEqualTo(Tags.SPAN_KIND_PRODUCER);
            assertThat(spanNode.getChildren()).hasSize(1);

            SpanNode child = spanNode.getChildren().get(0);
            assertThat(child.getTags()).hasSize(3);
            assertThat(child.operationName()).isEqualTo("jms-receive");
            assertThat(child.getTags().get(Tags.MESSAGE_BUS_DESTINATION.getKey())).isEqualTo("test");
            assertThat(child.getTags().get(Tags.SPAN_KIND.getKey())).isEqualTo(Tags.SPAN_KIND_CONSUMER);
            assertThat(child.getTags().get("jms.message.id")).isNotNull();
        }
    }

    @Inject
    Sender sender;

    @Inject
    Receiver receiver;

    @Inject
    private JMSContext context;

    @Inject
    Tracer tracer;

}
