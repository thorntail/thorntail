package io.thorntail.testsuite.vertx;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by bob on 6/21/18.
 */
@ApplicationScoped
public class Prefix {

    String getPrefix() {
        return "got it";
    }
}
