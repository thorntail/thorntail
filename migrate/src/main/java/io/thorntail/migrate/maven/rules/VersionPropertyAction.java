package io.thorntail.migrate.maven.rules;

import io.thorntail.migrate.maven.ModelAction;
import io.thorntail.migrate.maven.ModelRule;
import org.apache.maven.model.Model;
import io.thorntail.Info;

/**
 * Created by bob on 3/14/18.
 */
public class VersionPropertyAction implements ModelAction {

    public VersionPropertyAction(VersionPropertyRule rule) {
        this.rule = rule;
    }

    @Override
    public void apply(Model model) {
        model.getProperties().setProperty( "version." + Info.KEY, Info.VERSION);
        model.getProperties().remove( "version.wildfly.swarm");
        model.getProperties().remove( "version.wildfly-swarm");
    }

    @Override
    public ModelRule getRule() {
        return this.rule;
    }

    private final VersionPropertyRule rule;
}
