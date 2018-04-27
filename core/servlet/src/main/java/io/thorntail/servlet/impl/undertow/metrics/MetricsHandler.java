package io.thorntail.servlet.impl.undertow.metrics;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Timer;

/**
 * Created by bob on 1/23/18.
 */
public class MetricsHandler implements HttpHandler {

    MetricsHandler(String deploymentName, MetricRegistry registry, HttpHandler next) {
        this.next = next;
        this.deploymentName = deploymentName;
        this.registry = registry;
        this.request = counter("request", (meta) -> {
            meta.setDescription("Total request count");
        });
        this.response1xx = counter("response.1xx", (meta) -> {
            meta.setDescription("Count of responses with status code of 1xx ");
        });
        this.response2xx = counter("response.2xx", (meta) -> {
            meta.setDescription("Count of responses with status code of 2xx ");
        });
        this.response3xx = counter("response.3xx", (meta) -> {
            meta.setDescription("Count of responses with status code of 3xx ");
        });
        this.response4xx = counter("response.4xx", (meta) -> {
            meta.setDescription("Count of responses with status code of 4xx ");
        });
        this.response5xx = counter("response.5xx", (meta) -> {
            meta.setDescription("Count of responses with status code of 5xx ");
        });
        this.responseTime = timer("response", (meta) -> {
            meta.setDescription("Response time for all requests");
            meta.setDisplayName(deploymentName + " Response Time ");
        });
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        long startTick = System.currentTimeMillis();
        this.request.inc();
        exchange.addExchangeCompleteListener((completed, nextListener) -> {
            int baseStatusCode = completed.getStatusCode() / 100;
            if (baseStatusCode == 1) {
                this.response1xx.inc();
            } else if (baseStatusCode == 2) {
                this.response2xx.inc();
            } else if (baseStatusCode == 3) {
                this.response3xx.inc();
            } else if (baseStatusCode == 4) {
                this.response4xx.inc();
            } else if (baseStatusCode == 5) {
                this.response5xx.inc();
            }
            this.responseTime.update(System.currentTimeMillis() - startTick, TimeUnit.MILLISECONDS);
            nextListener.proceed();
        });
        this.next.handleRequest(exchange);

    }

    private Counter counter(String name, Consumer<Metadata> consumer) {
        Metadata meta = new Metadata(metricName(name), MetricType.COUNTER);
        consumer.accept(meta);
        return registry.counter(meta);
    }

    private Timer timer(String name, Consumer<Metadata> consumer) {
        Metadata meta = new Metadata(metricName(name), MetricType.TIMER);
        consumer.accept(meta);
        return registry.timer(meta);
    }

    private String metricName(String name) {
        return "deployment." + this.deploymentName + "." + name;
    }

    private final HttpHandler next;

    private final String deploymentName;

    private final MetricRegistry registry;

    private final Counter request;

    private final Counter response1xx;

    private final Counter response2xx;

    private final Counter response3xx;

    private final Counter response4xx;

    private final Counter response5xx;

    private final Timer responseTime;
}
