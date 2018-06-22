package io.thorntail.testsuite.vertx;

import javax.inject.Inject;

import io.thorntail.test.ThorntailTestRunner;
import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bob on 6/22/18.
 */
@RunWith(ThorntailTestRunner.class)
public class DnsClientTest {

    @Test
    public void testDnsClient() {
        assertThat( this.dnsClient).isNotNull();
    }

    @Inject
    DnsClient dnsClient;
}
