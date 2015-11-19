package org.wildfly.swarm.examples.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Bob McWhirter
 */
@Path("/")
public class MyResource {

    @GET
    @Produces("text/plain")
    public String get() throws NamingException, JMSException {
        Context ctx = new InitialContext();
        ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
        Connection conn = factory.createConnection();

        Topic topic = (Topic) ctx.lookup("/jms/topic/my-topic");

        Session sess = conn.createSession();
        MessageProducer prod = sess.createProducer(topic);
        TextMessage message = sess.createTextMessage();
        message.setText("Hello!");
        prod.send(message);
        sess.close();
        return "Howdy!";
    }
}
