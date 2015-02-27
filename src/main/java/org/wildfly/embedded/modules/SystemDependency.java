package org.wildfly.embedded.modules;

import org.jboss.modules.DependencySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class SystemDependency extends Dependency<SystemDependency> {

    private List<String> paths = new ArrayList<>();

    SystemDependency() {
    }

    public SystemDependency path(String path) {
        this.paths.add( path );
        return this;
    }

    DependencySpec getDependencySpec() {
        return null;
    }
}
