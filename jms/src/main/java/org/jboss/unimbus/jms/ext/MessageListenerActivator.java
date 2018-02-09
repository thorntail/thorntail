package org.jboss.unimbus.jms.ext;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.MessageListener;

import org.jboss.unimbus.jms.MessageDriven;

/**
 * Created by bob on 2/7/18.
 */
public class MessageListenerActivator {

    public MessageListenerActivator(Bean<MessageListener> listener, MessageDriven anno) {
        this.listener = listener;
        this.anno = anno;
    }

    public MessageDriven getAnnotation() {
        return this.anno;
    }

    public void activate(BeanManager beanManager) {
        JMSContext context = getJMSContext(beanManager);
        MessageListener listener = getMessageListener(beanManager);
        Destination destination = destination(context);
        this.consumer = context.createConsumer(destination);
        consumer.setMessageListener(listener);
    }

    JMSContext getJMSContext(BeanManager beanManager) {
        Set<Bean<?>> beans = beanManager.getBeans(JMSContext.class);
        Bean<JMSContext> bean = (Bean<JMSContext>) beanManager.resolve(beans);
        CreationalContext<JMSContext> context = beanManager.createCreationalContext(bean);
        return bean.create(context);
    }

    public MessageListener getMessageListener(BeanManager beanManager) {
        CreationalContext<MessageListener> context = beanManager.createCreationalContext(this.listener);
        return this.listener.create(context);
    }

    private Destination destination(JMSContext context) {
        if (!this.anno.topic().equals("")) {
            return context.createTopic(anno.topic());
        }
        return context.createQueue(anno.queue());
    }

    private final Bean<MessageListener> listener;

    private final MessageDriven anno;

    private JMSConsumer consumer;
}
