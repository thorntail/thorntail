/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.undertow.runtime;

import category.CommunityOnly;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.wildfly.swarm.bootstrap.modules.MavenResolvers;
import org.wildfly.swarm.bootstrap.util.JarFileManager;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.descriptors.CertInfo;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class CertInfoProducerTest {

    @After
    public void tearDown() throws Exception {
        JarFileManager.INSTANCE.close();
        MavenResolvers.close();
        TempFileManager.INSTANCE.close();
    }

    @Test
    public void testDefaults() {
        CertInfoProducer producer = new CertInfoProducer();
        producer.undertow = new UndertowFraction();

        CertInfo certInfo = producer.produceCertInfo();

        assertThat(certInfo).isNotNull();

        assertThat( certInfo.generateSelfSignedCertificateHost()).isNull();
    }

    @Test
    @Category(CommunityOnly.class)
    public void testGenerateWithDefaults() {
        CertInfoProducer producer = new CertInfoProducer();
        producer.undertow = new UndertowFraction();
        producer.generateSelfCertificate.set(true);

        CertInfo certInfo = producer.produceCertInfo();

        assertThat(certInfo).isNotNull();

        assertThat( certInfo.generateSelfSignedCertificateHost()).isEqualTo( "localhost" );
    }

    @Test
    @Category(CommunityOnly.class)
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
