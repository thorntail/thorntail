package org.jboss.unimbus.testsuite.jaxrs.jsonp;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.unimbus.UNimbus;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/")
public class MyJaxrsApplication extends Application {
    public static void main(String... args) throws Exception {
        UNimbus.run();
    }
}
