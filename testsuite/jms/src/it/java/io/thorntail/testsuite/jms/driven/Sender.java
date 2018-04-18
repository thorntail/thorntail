package io.thorntail.testsuite.jms.driven;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Destination;
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
        this.destination = this.context.createTopic("driven");
        this.producer = context.createProducer();
    }

    void send(String body) {
        this.producer.send(this.destination, body);
    }

    @Inject
    private JMSContext context;

    private JMSProducer producer;

    private Destination destination;
}
