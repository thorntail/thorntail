package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassPresent("org.jboss.unimbus.jpa.EntityManagerProducer")
public class OneNeededFailBean {
}
