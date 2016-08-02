package org.wildfly.swarm.topology.consul;

import java.net.MalformedURLException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Pre;
import org.wildfly.swarm.spi.api.annotations.ConfigurationValue;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class ConsultURLCustomizer implements Customizer {

    @Inject
    @Any
    ConsulTopologyFraction fraction;

    @Inject
    @ConfigurationValue("swarm.consul.url")
    private String consulUrl;


    @Override
    public void customize() {

        if ( this.consulUrl != null ) {
            try {
                this.fraction.url( this.consulUrl );
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

    }
}
