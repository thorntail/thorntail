package io.thorntail.testsuite.jaxrs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.thorntail.Thorntail;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/")
public class MyJaxrsApplication extends Application {
    public static void main(String... args) throws Exception {
        Thorntail.run();
    }
}
