package org.wildfly.swarm.internal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

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
