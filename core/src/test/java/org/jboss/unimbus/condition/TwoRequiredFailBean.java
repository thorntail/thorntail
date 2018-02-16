package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.condition.annotation.RequiredClassPresent;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassPresent("io.restassured.RestAssured")
@RequiredClassPresent("org.jboss.unimbus.jpa.EntityManagerProducer")
public class TwoRequiredFailBean {
}
