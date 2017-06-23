package org.wildfly.swarm.security;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.junit.Test;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.config.security.security_domain.authentication.AuthModule;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 6/23/17.
 */
public class SecurityFractionTest {

    @Test
    public void testCorrectOrderingOfAuthModules() throws Exception {
        SecurityFraction fraction = new SecurityFraction();

        fraction.securityDomain("jaspioauth", (domain) -> {
            domain.jaspiAuthentication((authn) -> {
                authn.authModule("2-OAuth", (module) -> {
                    // nothing
                });
                authn.authModule("1-JWT", (module) -> {
                    // nothing
                });
            });
        });

        List<AuthModule> modules = fraction.subresources().securityDomain("jaspioauth").subresources().jaspiAuthentication().subresources().authModules();

        assertThat(modules).hasSize(2);
        assertThat(modules.get(0).getKey()).isEqualTo("2-OAuth");
        assertThat(modules.get(1).getKey()).isEqualTo("1-JWT");

        List<ModelNode> result = Marshaller.marshal(fraction);

        PathAddress baseAddr = PathAddress.pathAddress("subsystem", "security")
                .append("security-domain", "jaspioauth")
                .append("authentication", "jaspi");

        List<PathAddress> addresses = result.stream()
                .map(e -> PathAddress.pathAddress(e.get("address")))
                .filter(e -> e.getParent().equals(baseAddr))
                .collect(Collectors.toList());

        assertThat( addresses.get(0).getLastElement().getValue()).isEqualTo("2-OAuth");
        assertThat( addresses.get(1).getLastElement().getValue()).isEqualTo("1-JWT");
    }
}
