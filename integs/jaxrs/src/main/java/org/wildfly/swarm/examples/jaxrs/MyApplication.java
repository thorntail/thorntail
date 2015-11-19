package org.wildfly.swarm.examples.jaxrs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author Bob McWhirter
 */
@ApplicationPath("/")
public class MyApplication extends Application {

    public MyApplication() {
    }
}
