package org.wildfly.swarm.jolokia.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jolokia.JolokiaFraction;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 */
public class JolokiaConfiguration extends AbstractServerConfiguration<JolokiaFraction> {

    public JolokiaConfiguration() {
        super(JolokiaFraction.class);
    }

    @Override
    public JolokiaFraction defaultFraction() {
        System.err.println( "create default fraction for Jolokia" );
        return new JolokiaFraction();
    }

    @Override
    public List<Archive> getImplicitDeployments(JolokiaFraction fraction) {
        List<Archive> list =new ArrayList<>();
        JavaArchive war = null;
        try {
            war = Swarm.artifact("org.jolokia:jolokia-war:war:*");
            war.as(WARArchive.class).setContextRoot( fraction.context() );
            list.add(war);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return list;
    }
}
