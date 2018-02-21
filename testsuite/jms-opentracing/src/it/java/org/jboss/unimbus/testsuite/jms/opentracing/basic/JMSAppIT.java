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
import org.jboss.unimbus.test.EphemeralPorts;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.jboss.unimbus.testsuite.jms.opentracing.util.SpanNode;
import org.jboss.unimbus.testsuite.jms.opentracing.util.SpanTree;
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

        System.err.println( tree );
        assertThat(tree.getRootNodes()).hasSize(1);

        SpanNode root = tree.getRootNodes().get(0);
        assertThat(root.getChildren()).hasSize(3);

        for (SpanNode spanNode : root.getChildren()) {
            assertThat(spanNode.getTags()).hasSize(1);
            assertThat(spanNode.operationName()).isEqualTo("send");
            assertThat(spanNode.getTags().get("jms.destination")).isEqualTo("test");
            assertThat(spanNode.getChildren()).hasSize(1);
            SpanNode child = spanNode.getChildren().get(0);
            assertThat(child.getTags()).hasSize(2);
            assertThat(child.operationName()).isEqualTo("receive");
            assertThat(child.getTags().get("jms.destination")).isEqualTo("test");
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
