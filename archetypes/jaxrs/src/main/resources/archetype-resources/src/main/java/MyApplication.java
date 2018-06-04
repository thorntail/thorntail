package ${package};

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.thorntail.Thorntail;

@ApplicationPath("/")
public class MyApplication extends Application {
    public static void main(String... args) throws Exception {
        Thorntail.run();
    }
}
