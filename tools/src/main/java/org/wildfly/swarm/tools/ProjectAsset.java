package org.wildfly.swarm.tools;

import org.jboss.shrinkwrap.api.asset.NamedAsset;

/**
 * @author Bob McWhirter
 */
public interface ProjectAsset extends NamedAsset {

    String getSimpleName();

    default String getName()  {
        return "_bootstrap/" + getSimpleName();
    }
}
