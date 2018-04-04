package org.jboss.unimbus.testsuite.jpajta;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.unimbus.UNimbus;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/")
public class MyJpaJtaApplication extends Application {
    public static void main(String... args) throws Exception {
        UNimbus.run();
    }
}
