package org.jboss.unimbus.condition;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
@RequiredClassPresent("org.jboss.unimbus.config.impl.ConfigImpl")
public class OneNeededBean {
}
