package io.thorntail.test.impl;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.restassured.RestAssured;
import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.servlet.annotation.Primary;

/**
 * Created by bob on 1/23/18.
 */
@ApplicationScoped
@RequiredClassPresent("io.thorntail.servlet.annotation.Primary")
@RequiredClassPresent("io.restassured.RestAssured")
public class RestAssuredInitializer {

    void setupRestAssured(@Observes LifecycleEvent.AfterStart event) {
        RestAssured.baseURI = this.primaryUrl.toExternalForm();
    }

    @Inject
    @Primary
    private URL primaryUrl;
}
