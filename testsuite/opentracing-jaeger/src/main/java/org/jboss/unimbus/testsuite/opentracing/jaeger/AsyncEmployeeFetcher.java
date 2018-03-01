package org.jboss.unimbus.testsuite.opentracing.jaeger;

import java.io.Serializable;
import java.util.List;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.eclipse.microprofile.opentracing.Traced;

/**
 * Created by bob on 3/1/18.
 */
@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "employees"),
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
                @ActivationConfigProperty(propertyName = "useJndi", propertyValue = "false"),
        }
)
@Traced
public class AsyncEmployeeFetcher implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            Destination replyTo = message.getJMSReplyTo();
            List<Employee> employees = em.createNamedQuery("Employee.findAll", Employee.class).getResultList();
            this.context.createProducer().send( replyTo, (Serializable) employees);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @PersistenceContext
    private EntityManager em;

    @Inject
    JMSContext context;
}
