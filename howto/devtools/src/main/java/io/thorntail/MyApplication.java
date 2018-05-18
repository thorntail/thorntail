package io.thorntail;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class MyApplication extends Application {
    public static void main(String... args) throws Exception {
        Main.main(args);
    }
}
