package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassNotPresent("org.jboss.unimbus.jpa.EntityManagerProducer")
@RequiredClassNotPresent("org.jboss.unimbus.servlet.Primary")
public class TwoMissingToBeActiveBean {
}
