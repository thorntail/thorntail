package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassPresent("io.restassured.RestAssured")
@RequiredClassPresent("org.jboss.unimbus.config.impl.ConfigImpl")
public class TwoRequiredBean {
}
