package org.example;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.resource.ResourceException;

import io.vertx.core.eventbus.Message;
import io.vertx.resourceadapter.VertxConnection;
import io.vertx.resourceadapter.VertxConnectionFactory;
import io.vertx.resourceadapter.inflow.VertxListener;
import org.jboss.ejb3.annotation.ResourceAdapter;

/**
 * Created by ggastald on 13/06/16.
 */
@MessageDriven(name = "VertxMonitor",
        messageListenerInterface = VertxListener.class,
        activationConfig = {
                @ActivationConfigProperty(propertyName = "address", propertyValue = "tacos")
        })
@ResourceAdapter("vertx-ra")
public class MyListener implements VertxListener{

    @Resource(mappedName = "java:/eis/VertxConnectionFactory")
    VertxConnectionFactory connectionFactory;

    @Override
    public <String> void onMessage(Message<String> message) {

        System.out.println("MESSAGE: "+message.body());
        System.out.println("Sending to nachos...");
        try (VertxConnection connection = connectionFactory.getVertxConnection()) {
            connection.vertxEventBus().send("nachos","EAT NACHOS!");
        } catch (ResourceException e) {
            e.printStackTrace();
        }
    }
}
