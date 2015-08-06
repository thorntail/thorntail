package org.wildfly.swarm.runtime.netflix.ribbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netflix.loadbalancer.AbstractServerList;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.netflix.ribbon.RibbonFraction;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;
import org.wildfly.swarm.runtime.container.ServerConfiguration;

/**
 * @author Bob McWhirter
 */
public class RibbonConfiguration extends AbstractServerConfiguration<RibbonFraction> {

    public RibbonConfiguration() {
        super(RibbonFraction.class);
    }

    @Override
    public RibbonFraction defaultFraction() {
        return new RibbonFraction();
    }

    @Override
    public List<ModelNode> getList(RibbonFraction fraction) {
        return Collections.emptyList();
    }

    @Override
    public List<ServiceActivator> getServiceActivators(RibbonFraction fraction) {
        List<ServiceActivator> activators = new ArrayList<>();
        activators.add( new ClusterManagerActivator() );
        return activators;
    }

    @Override
    public void prepareArchive(Archive archive) {
        archive.as(JARArchive.class).addModule("org.wildfly.swarm.netflix.ribbon.client");
        archive.as(JARArchive.class).addModule("org.wildfly.swarm.netflix.ribbon", "runtime");
        archive.as(JARArchive.class).addModule("com.netflix.ribbon" );
        archive.as(JARArchive.class).addModule("io.reactivex.rxjava" );
        archive.as(JARArchive.class).addModule("io.netty" );
    }
}
