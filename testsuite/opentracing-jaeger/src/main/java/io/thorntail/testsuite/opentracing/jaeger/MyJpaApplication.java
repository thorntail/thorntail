package io.thorntail.testsuite.opentracing.jaeger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.thorntail.Thorntail;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/")
public class MyJpaApplication extends Application {
    public static void main(String... args) throws Exception {
        Thorntail.run();
    }
}
