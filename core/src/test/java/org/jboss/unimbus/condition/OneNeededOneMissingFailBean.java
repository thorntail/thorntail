package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.condition.annotation.RequiredClassNotPresent;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassNotPresent("org.jboss.unimbus.servlet.Primary")
@RequiredClassPresent("org.jboss.unimbus.jpa.EntityManagerProducer")
public class OneNeededOneMissingFailBean {
}
