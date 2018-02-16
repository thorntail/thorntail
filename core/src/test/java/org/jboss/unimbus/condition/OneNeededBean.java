package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.condition.annotation.RequiredClassPresent;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassPresent("org.jboss.unimbus.config.impl.ConfigImpl")
public class OneNeededBean {
}
