package io.thorntail.condition;

import javax.enterprise.context.ApplicationScoped;

import io.thorntail.condition.annotation.RequiredClassNotPresent;

import static io.thorntail.Info.ROOT_PACKAGE;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassNotPresent(ROOT_PACKAGE + ".jpa.EntityManagerProducer")
@RequiredClassNotPresent(ROOT_PACKAGE + ".servlet.Primary")
public class TwoMissingToBeActiveBean {
}
