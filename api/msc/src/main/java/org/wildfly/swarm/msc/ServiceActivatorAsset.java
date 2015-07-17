package org.wildfly.swarm.msc;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class ServiceActivatorAsset implements Asset {

    private List<String> activators = new ArrayList<>();

    public ServiceActivatorAsset() {

    }

    public void addServiceActivator(String className) {
        this.activators.add( className );
    }

    public void addServiceActivator(Class<? extends ServiceActivator> cls) {
        this.activators.add( cls.getName() );
    }

    @Override
    public InputStream openStream() {
        StringBuilder builder = new StringBuilder();

        for (String activator : this.activators) {
            builder.append( activator ).append( "\n" );
        }

        return new ByteArrayInputStream( builder.toString().getBytes() );

    }
}
