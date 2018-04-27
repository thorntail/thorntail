package io.thorntail.migrate.maven;

import org.apache.maven.model.Dependency;

/**
 * Created by bob on 3/13/18.
 */
public abstract class DependencyAction<T extends DependencyRule> implements ModelAction {

    public DependencyAction(T rule, Dependency dependency) {
        this.rule = rule;
        this.dependency = dependency;
    }

    @Override
    public T getRule() {
        return this.rule;
    }

    private final T rule;
    protected final Dependency dependency;

}
