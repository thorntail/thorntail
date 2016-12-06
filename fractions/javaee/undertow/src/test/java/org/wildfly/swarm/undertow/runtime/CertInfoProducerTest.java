package org.wildfly.swarm.undertow.runtime;

import org.junit.Test;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.descriptors.CertInfo;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class CertInfoProducerTest {

    @Test
    public void testDefaults() {
        CertInfoProducer producer = new CertInfoProducer();
        producer.undertow = new UndertowFraction();

        CertInfo certInfo = producer.produceCertInfo();

        assertThat(certInfo).isNotNull();

        assertThat( certInfo.generateSelfSignedCertificateHost()).isNull();
    }

    @Test
    public void testGenerateWithDefaults() {
        CertInfoProducer producer = new CertInfoProducer();
        producer.undertow = new UndertowFraction();
        producer.generateSelfCertificate.set(true);

        CertInfo certInfo = producer.produceCertInfo();

        assertThat(certInfo).isNotNull();

        assertThat( certInfo.generateSelfSignedCertificateHost()).isEqualTo( "localhost" );
    }

    @Test
    public void testGenerateWithExplicitHost() {
        CertInfoProducer producer = new CertInfoProducer();
        producer.undertow = new UndertowFraction();
        producer.generateSelfCertificate.set(true);
        producer.selfCertificateHost.set( "www.mycorp.com" );

        CertInfo certInfo = producer.produceCertInfo();

        assertThat(certInfo).isNotNull();

        assertThat( certInfo.generateSelfSignedCertificateHost()).isEqualTo( "www.mycorp.com" );
    }
}
