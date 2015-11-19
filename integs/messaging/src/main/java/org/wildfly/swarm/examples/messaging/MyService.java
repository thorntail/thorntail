package org.wildfly.swarm.examples.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class MyService implements Service<Void> {

    private final String destinationName;

    private Connection conn;

    public MyService(String destinationName) {
        this.destinationName = destinationName;
    }

    public void start(final StartContext startContext) throws StartException {
        try {
            Context ctx = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
            this.conn = factory.createConnection();

            Destination destination = (Destination) ctx.lookup(destinationName);
            Session sess = conn.createSession();
            MessageConsumer consumer = sess.createConsumer(destination);
            startContext.complete();
            System.err.println("Starting to receive from " + destination);
            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    try {
                        System.err.println("received: " + ((TextMessage) message).getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
            conn.start();
        } catch (Throwable t) {
            throw new StartException(t);
        }
    }

    public void stop(StopContext stopContext) {
        try {
            this.conn.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }
}
