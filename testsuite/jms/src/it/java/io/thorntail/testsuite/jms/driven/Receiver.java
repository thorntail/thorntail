package io.thorntail.testsuite.jms.driven;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.eclipse.microprofile.opentracing.Traced;


/**
 * Created by bob on 2/7/18.
 */
@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "driven"),
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
                @ActivationConfigProperty(propertyName = "useJndi", propertyValue = "false"),
        }
)
@Traced
public class Receiver implements MessageListener {


    @Override
    public void onMessage(Message message) {
        try {
            this.results.add(message.getBody(String.class));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


    @Inject
    Results results;
}
