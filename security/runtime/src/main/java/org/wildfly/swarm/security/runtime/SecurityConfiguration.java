package org.wildfly.swarm.security.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.config.security.security_domain.ClassicAuthentication;
import org.wildfly.swarm.config.security.security_domain.authentication.LoginModule;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.security.SecurityFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public class SecurityConfiguration extends AbstractServerConfiguration<SecurityFraction> {

    public SecurityConfiguration() {
        super(SecurityFraction.class);
    }

    @Override
    public SecurityFraction defaultFraction() {
        return new SecurityFraction()
                .securityDomain(new SecurityDomain("other")
                        .classicAuthentication(new ClassicAuthentication()
                                .loginModule(new LoginModule("RealmDirect")
                                                .code("RealmDirect")
                                                .flag("required")
                                                .moduleOptions(new HashMap() {{
                                                    put("password-stacking", "useFirstPass");
                                                }})

                                )));
    }

    @Override
    public List<ModelNode> getList(SecurityFraction fraction) {
        if (fraction == null) {
            fraction = defaultFraction();
        }

        List<ModelNode> list = new ArrayList<>();

        ModelNode address = new ModelNode();

        address.setEmptyList();

        ModelNode add = new ModelNode();
        add.get(OP_ADDR).set(address).add(EXTENSION, "org.jboss.as.security");
        add.get(OP).set(ADD);
        list.add(add);
        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure Security subsystem. " + e);
        }

        return list;
    }
}
