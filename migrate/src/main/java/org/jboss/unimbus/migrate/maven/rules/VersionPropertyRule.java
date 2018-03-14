package org.jboss.unimbus.migrate.maven.rules;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.maven.model.Model;
import org.jboss.unimbus.migrate.maven.ModelAction;
import org.jboss.unimbus.migrate.maven.ModelRule;

/**
 * Created by bob on 3/14/18.
 */
@ApplicationScoped
public class VersionPropertyRule implements ModelRule {
    @Override
    public List<? extends ModelAction> match(Model context) {
        return Collections.singletonList(new VersionPropertyAction(this));
    }
}
