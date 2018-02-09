package org.jboss.unimbus.jms;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.jms.ext.MessageListenerActivator;

/**
 * Created by bob on 2/7/18.
 */
@ApplicationScoped
public class MessageListenerRegistry {

    void init(@Observes LifecycleEvent.Deploy event) {
        this.activators.forEach(e -> {
            //this.consumers.add(consumer(e));
            e.activate(this.beanManager);
        });
    }

    /*
    private JMSConsumer consumer(MessageListenerActivator activator) {
        JMSConsumer consumer = this.context.createConsumer(destination(activator));
        consumer.setMessageListener(activator.getMessageListener(this.beanManager));
        return consumer;
    }

    private Destination destination(MessageListenerActivator activator) {
        MessageDriven anno = activator.getAnnotation();
        if (!anno.topic().equals("")) {
            return this.context.createTopic(anno.topic());
        }
        return this.context.createQueue(anno.queue());
    }
    */

    @Inject
    @Any
    Instance<MessageListenerActivator> activators;

    //@Inject
    //JMSContext context;

    @Inject
    BeanManager beanManager;

}
