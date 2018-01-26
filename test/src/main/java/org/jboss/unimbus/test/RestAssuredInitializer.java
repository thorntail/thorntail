package org.jboss.unimbus.test;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.restassured.RestAssured;
import org.jboss.unimbus.condition.RequiredClassPresent;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.Primary;

/**
 * Created by bob on 1/23/18.
 */
@ApplicationScoped
@RequiredClassPresent("org.jboss.unimbus.servlet.Primary")
@RequiredClassPresent("io.restassured.RestAssured")
public class RestAssuredInitializer {

    void setupRestAssured(@Observes LifecycleEvent.AfterStart event) {
        RestAssured.baseURI = this.primaryUrl.toExternalForm();
    }

    @Inject
    @Primary
    private URL primaryUrl;
}
