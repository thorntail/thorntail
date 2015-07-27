package org.wildfly.swarm.runtime.netflix.ribbon.secured;

import java.util.Collections;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.netflix.ribbon.secured.RibbonSecuredFraction;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

/**
 * @author Bob McWhirter
 */
public class RibbonSecuredConfiguration extends AbstractServerConfiguration<RibbonSecuredFraction> {

    public RibbonSecuredConfiguration() {
        super(RibbonSecuredFraction.class);
    }

    @Override
    public RibbonSecuredFraction defaultFraction() {
        return new RibbonSecuredFraction();
    }

    @Override
    public List<ModelNode> getList(RibbonSecuredFraction fraction) {
        return Collections.emptyList();
    }

    @Override
    public void prepareArchive(Archive archive) {
        archive.as(JARArchive.class).addModule("org.wildfly.swarm.netflix.ribbon.secured.client");
    }
}
