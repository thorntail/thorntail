package io.thorntail.testsuite.opentracing.jaeger;

import java.util.List;

import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.ext.ExceptionMapper;

import com.uber.jaeger.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.eclipse.microprofile.opentracing.Traced;

/**
 * Created by bob on 3/1/18.
 */
@Path("/")
public class Employees {

    @Path("/start")
    @GET
    public String start() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080/").path("/employees");

        String response = target.request().get(String.class);
        return currentTraceId() + "|" + response;
    }

    @Traced
    @Path("/employees")
    @GET
    public void getEmployees(@Suspended AsyncResponse response) throws Exception {
        TemporaryQueue replyTo = this.context.createTemporaryQueue();

        JMSProducer producer = this.context.createProducer();
        TextMessage request = this.context.createTextMessage("fetch");
        request.setJMSReplyTo(replyTo);
        producer.send(this.context.createTopic("employees"), request);

        JMSConsumer consumer = this.context.createConsumer(replyTo);
        new Thread(() -> {
            List results = consumer.receiveBody(List.class);
            response.resume(results);
        }).start();
    }

    private String currentTraceId() {
        Tracer tracer = GlobalTracer.get();
        SpanContext ctx = (SpanContext) tracer.activeSpan().context();
        return Long.toHexString(ctx.getTraceId());
    }

    @Inject
    JMSContext context;
}
