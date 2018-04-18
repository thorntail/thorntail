package io.thorntail.testsuite.jms.opentracing.basic;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;

/**
 * Created by bob on 2/7/18.
 */
@ApplicationScoped
public class Sender {

    @PostConstruct
    void init() {
        this.queue = this.context.createQueue("test");
        this.producer = context.createProducer();
    }

    void send(String body) {
        this.producer.send(this.queue, body);
    }


    @Inject
    private JMSContext context;

    private JMSProducer producer;

    private Queue queue;
}
