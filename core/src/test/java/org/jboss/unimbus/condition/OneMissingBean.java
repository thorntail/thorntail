package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.condition.annotation.RequiredClassNotPresent;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassNotPresent("org.jboss.unimbus.servlet.Primary")
public class OneMissingBean {
}
