package org.wildfly.swarm.netflix.ribbon;

import org.jboss.shrinkwrap.api.Assignable;

/**
 * @author Bob McWhirter
 */
public interface RibbonArchive extends Assignable {
    void setApplicationName(String name);
}
