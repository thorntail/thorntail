package io.thorntail.testsuite.vertx;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

import io.vertx.core.eventbus.Message;
import io.vertx.resourceadapter.inflow.VertxListener;


/**
 * Created by bob on 2/7/18.
 */
@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "address", propertyValue = "driven.event.address"),
        }
)
public class Receiver implements VertxListener {

    @Override
    public <T> void onMessage(Message<T> message) {
        this.results.add(message.body().toString());
    }


    @Inject
    Results results;
}
