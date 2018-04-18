package io.thorntail.testsuite.jpajta;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.thorntail.Thorntail;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/")
public class MyJpaJtaApplication extends Application {
    public static void main(String... args) throws Exception {
        Thorntail.run();
    }
}
