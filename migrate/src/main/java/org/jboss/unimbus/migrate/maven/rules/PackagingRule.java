package org.jboss.unimbus.migrate.maven.rules;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.maven.model.Model;
import org.jboss.unimbus.migrate.maven.ModelAction;
import org.jboss.unimbus.migrate.maven.ModelRule;
import org.jboss.unimbus.migrate.maven.PackagingAction;

/**
 * Created by bob on 3/13/18.
 */
@ApplicationScoped
public class PackagingRule implements ModelRule {

    @Override
    public List<ModelAction> match(Model context) {
        if ( context.getPackaging().equals("war")) {
            return Collections.singletonList( new PackagingAction(this) );
        }
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "change packaging from 'war' to 'jar'";
    }
}
