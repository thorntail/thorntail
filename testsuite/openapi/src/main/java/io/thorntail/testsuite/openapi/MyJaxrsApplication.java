package io.thorntail.testsuite.openapi;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.thorntail.Thorntail;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 4/20/18
 */
@ApplicationPath("/")
public class MyJaxrsApplication extends Application {

    @Override
    public Set<Object> getSingletons() {
        return Stream.of(new ResourceAcceptingArray(), new ResourceAcceptingList())
                .collect(Collectors.toSet());
    }

    public static void main(String... args) throws Exception {
        Thorntail.run();
    }
}
