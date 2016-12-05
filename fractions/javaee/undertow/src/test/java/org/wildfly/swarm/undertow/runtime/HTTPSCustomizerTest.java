package org.wildfly.swarm.undertow.runtime;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.junit.Test;
import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.management.SecurityRealm;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.server.HTTPListener;
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
    public void testWithManagementFraction() {
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
        assertThat( realm.subresources().sslServerIdentity().generateSelfSignedCertificateHost() ).isEqualTo( "myhost.com" );
    }
}
