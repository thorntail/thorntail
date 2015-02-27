package org.wildfly.embedded.modules;

import org.jboss.modules.DependencySpec;

/**
 * @author Bob McWhirter
 */
public class ModuleDependency extends Dependency<ModuleDependency> {

    public enum Services {
        IMPORT,
        EXPORT,
        NONE,
    }

    public Services services = Services.NONE;
    public final String name;
    public final String slot;
    public boolean optional;

    ModuleDependency(String name, String slot) {
        this.name = name;
        this.slot = slot;
    }

    public ModuleDependency services(Services services) {
        this.services = services;
        return this;
    }

    public ModuleDependency optional() {
        this.optional = true;
        return this;
    }

    DependencySpec getDependencySpec() {
        return null;
    }
}
