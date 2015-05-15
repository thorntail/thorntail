package org.wildfly.swarm.msc;

import org.jboss.modules.Module;
import org.jboss.msc.service.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class ServiceDeploymentRegistry {

    private final static Map<String,ServiceDeploymentRegistry> REGISTRY = new HashMap<>();
    private final List<Service> services = new ArrayList<>();

    public static ServiceDeploymentRegistry get(String name) {
        ServiceDeploymentRegistry registry = REGISTRY.get(name);
        if ( registry == null ) {
            registry = new ServiceDeploymentRegistry();
            REGISTRY.put( name, registry );
        }

        return registry;
    }

    public static ServiceDeploymentRegistry get() throws IOException {
        return REGISTRY.get( System.getProperty( "wildfly.swarm.current.deployment") );
    }


    public ServiceDeploymentRegistry() {
    }

    public void addService(Service service) {
        this.services.add( service );
    }

    public List<Service> getServices() {
        return this.services;
    }
}
