package org.wildfly.embedded.modules;

import org.jboss.modules.DependencySpec;

/**
 * @author Bob McWhirter
 */
abstract class Dependency<T extends Dependency> {

    protected boolean export;

    abstract DependencySpec getDependencySpec();

    public T export() {
        this.export = true;
        return (T) this;
    }

}
