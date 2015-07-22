package org.wildfly.swarm.netflix.ribbon;

import org.jboss.shrinkwrap.api.asset.NamedAsset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Bob McWhirter
 */
public class RibbonConfigAsset implements NamedAsset {
    public static final String NAME = "WEB-INF/classes/config.properties";

    public RibbonConfigAsset() {

    }

    @Override
    public InputStream openStream() {
        StringBuilder str = new StringBuilder();

        str.append( "ribbon.NIWSServerListClassName:org.wildfly.swarm.runtime.netflix.ribbon.ClusterServerList\n");
        str.append( "ribbon.NFLoadBalancerRuleClassName=com.netflix.loadbalancer.RoundRobinRule\n");

        return new ByteArrayInputStream( str.toString().getBytes() );
    }

    @Override
    public String getName() {
        return NAME;
    }
}
