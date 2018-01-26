package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassPresent("io.restassured.RestAssured")
@RequiredClassPresent("org.jboss.unimbus.jpa.EntityManagerProducer")
public class TwoRequiredFailBean {
}
