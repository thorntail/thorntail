package io.thorntail.condition;

import javax.enterprise.context.ApplicationScoped;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.condition.annotation.RequiredClassNotPresent;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassNotPresent("io.thorntail.servlet.Primary")
@RequiredClassPresent("io.thorntail.config.impl.InterpolatingConfig")
public class OneNeededOneMissingBean {
}
