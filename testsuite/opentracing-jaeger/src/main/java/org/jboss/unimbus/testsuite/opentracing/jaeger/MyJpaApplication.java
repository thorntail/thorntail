package org.jboss.unimbus.testsuite.opentracing.jaeger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.unimbus.UNimbus;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/")
public class MyJpaApplication extends Application {
    public static void main(String... args) throws Exception {
        UNimbus.run();
    }
}
