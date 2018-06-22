package io.thorntail.vertx;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;

/**
 * Created by bob on 6/22/18.
 */
@Dependent
public class DnsClientProducer {

    @Produces
    @ApplicationScoped
    DnsClient dnsClient() {
        return vertx.createDnsClient();
    }

    @Inject
    private Vertx vertx;
}
