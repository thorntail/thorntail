package io.thorntail.condition;

import javax.enterprise.context.ApplicationScoped;

import io.thorntail.condition.annotation.RequiredClassPresent;

import static io.thorntail.Info.ROOT_PACKAGE;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassPresent("io.restassured.RestAssured")
@RequiredClassPresent(ROOT_PACKAGE + ".config.impl.InterpolatingConfig")
public class TwoRequiredBean {
}
