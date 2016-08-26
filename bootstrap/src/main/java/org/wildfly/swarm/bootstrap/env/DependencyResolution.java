package org.wildfly.swarm.bootstrap.env;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Heiko Braun
 * @since 18/07/16
 */
public interface DependencyResolution {
    Set<String> resolve(List<String> exclusions) throws IOException;

    default boolean excluded(Collection<String> exclusions, String classPathElement) {
        for (String exclusion : exclusions) {
            if (classPathElement.contains(exclusion)) {
                return true;
            }
        }

        return false;
    }


}
