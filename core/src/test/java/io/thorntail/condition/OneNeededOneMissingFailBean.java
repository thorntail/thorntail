package io.thorntail.condition;

import javax.enterprise.context.ApplicationScoped;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.condition.annotation.RequiredClassNotPresent;

import static io.thorntail.Info.ROOT_PACKAGE;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassNotPresent(ROOT_PACKAGE + ".servlet.Primary")
@RequiredClassPresent(ROOT_PACKAGE + ".jpa.EntityManagerProducer")
public class OneNeededOneMissingFailBean {
}
