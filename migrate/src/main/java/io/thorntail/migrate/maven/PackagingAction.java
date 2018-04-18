package io.thorntail.migrate.maven;

import org.apache.maven.model.Model;
import io.thorntail.migrate.maven.rules.PackagingRule;

/**
 * Created by bob on 3/13/18.
 */
public class PackagingAction implements ModelAction {

    public PackagingAction(PackagingRule rule) {
        this.rule = rule;
    }

    @Override
    public ModelRule getRule() {
        return this.rule;
    }

    @Override
    public void apply(Model model) {
        model.setPackaging("jar");
    }

    protected final PackagingRule rule;
}
