package io.thorntail.testsuite.opentracing.jaeger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import io.vertx.core.eventbus.Message;
import io.vertx.resourceadapter.inflow.VertxListener;
import org.eclipse.microprofile.opentracing.Traced;

/**
 * Created by bob on 3/1/18.
 */
@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "address", propertyValue = "fetch-logger"),
        }
)
@Traced
public class EmployeeFetchLogger implements VertxListener {

    @Override
    public <T> void onMessage(Message<T> message) {
        System.err.println("logged: " + message.body());
    }
}
