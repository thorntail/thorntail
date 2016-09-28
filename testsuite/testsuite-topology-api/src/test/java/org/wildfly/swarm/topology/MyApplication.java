package org.wildfly.swarm.topology;

import javax.naming.NamingException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author Bob McWhirter
 */
@ApplicationPath("/")
public class MyApplication extends Application {
    public MyApplication() throws NamingException {
        Topology.lookup().advertise( "tacos" );
    }
}
