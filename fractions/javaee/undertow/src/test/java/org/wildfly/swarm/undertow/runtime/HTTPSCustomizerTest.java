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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.management.SecurityRealm;
import org.wildfly.swarm.config.management.security_realm.SslServerIdentity;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.descriptors.CertInfo;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class HTTPSCustomizerTest {

    @Test
    public void testWithoutManagementFraction() {
        HTTPSCustomizer customizer = new HTTPSCustomizer();
        customizer.undertow = new UndertowFraction();
        customizer.undertow.applyDefaults();
        customizer.certInfo = CertInfo.INVALID;
        customizer.managementCoreService = new MockInstance<>(null);
        customizer.customize();

        Server server = customizer.undertow.subresources().server("default-server");

        assertThat( server ).isNotNull();

        assertThat( server.subresources().httpListeners() ).hasSize( 1 );
        assertThat( server.subresources().httpListener("default" ) ).isNotNull();

        assertThat( server.subresources().httpsListeners() ).isEmpty();
    }

    @Test
    public void testWithManagementFraction() throws Exception {
        HTTPSCustomizer customizer = new HTTPSCustomizer();
        customizer.undertow = new UndertowFraction();
        customizer.undertow.applyDefaults();
        customizer.certInfo = new CertInfo("myhost.com", "./my/path");
        customizer.managementCoreService = new MockInstance<>(new ManagementCoreService());
        customizer.customize();

        Server server = customizer.undertow.subresources().server("default-server");

        assertThat( server ).isNotNull();

        assertThat( server.subresources().httpListeners() ).hasSize( 1 );
        assertThat( server.subresources().httpListener("default" ) ).isNotNull();

        assertThat( server.subresources().httpsListeners() ).hasSize( 1 );
        assertThat( server.subresources().httpsListener("default-https" ) ).isNotNull();

        SecurityRealm realm = customizer.managementCoreService.get().subresources().securityRealm("SSLRealm");

        assertThat( realm ).isNotNull();

        assertThat( realm.subresources().sslServerIdentity().keystoreRelativeTo() ).isEqualTo( "./my/path" );
        assertSelfSignedCertificate(realm.subresources().sslServerIdentity(), "myhost.com");
    }

    private void assertSelfSignedCertificate(SslServerIdentity identity, String expectedResult) throws InvocationTargetException, IllegalAccessException {
        try {
            Method genMethod = identity.getClass().getMethod("generateSelfSignedCertificateHost");

            assertThat( genMethod.invoke(identity) ).isEqualTo( expectedResult );
        } catch (NoSuchMethodException e) {
            // Do Nothing. Just means the method doesn't exist on the Config API.
        }
    }
}
