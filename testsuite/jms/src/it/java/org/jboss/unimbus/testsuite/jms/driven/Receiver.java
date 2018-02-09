package org.jboss.unimbus.testsuite.jms.driven;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;


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
