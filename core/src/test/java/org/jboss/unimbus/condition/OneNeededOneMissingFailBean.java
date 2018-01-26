package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassNotPresent("org.jboss.unimbus.servlet.Primary")
@RequiredClassPresent("org.jboss.unimbus.jpa.EntityManagerProducer")
public class OneNeededOneMissingFailBean {
}
