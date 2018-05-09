package io.thorntail.howto;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.thorntail.Thorntail;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/")
public class MyApplication extends Application {
    public static void main(String... args) throws Exception {
        Thorntail.main(args);
    }
}
