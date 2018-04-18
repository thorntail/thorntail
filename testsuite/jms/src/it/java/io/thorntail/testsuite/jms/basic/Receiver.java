package io.thorntail.testsuite.jms.basic;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;

/**
 * Created by bob on 2/7/18.
 */
@ApplicationScoped
public class Receiver {

    @PostConstruct
    void init() {
        Queue queue = this.context.createQueue("test");
        this.consumer = this.context.createConsumer(queue);

    }

    public String receive() {
        return this.consumer.receiveBody(String.class, 5000);
    }

    @Inject
    private JMSContext context;


    private JMSConsumer consumer;
}
