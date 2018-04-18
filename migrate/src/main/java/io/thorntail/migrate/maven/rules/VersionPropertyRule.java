package io.thorntail.migrate.maven.rules;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.thorntail.migrate.maven.ModelAction;
import io.thorntail.migrate.maven.ModelRule;
import org.apache.maven.model.Model;

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
