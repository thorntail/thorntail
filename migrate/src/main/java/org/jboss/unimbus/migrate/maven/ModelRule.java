package org.jboss.unimbus.migrate.maven;

import java.util.List;

import org.apache.maven.model.Model;
import org.jboss.unimbus.migrate.Rule;

/**
 * Created by bob on 3/13/18.
 */
public interface ModelRule extends Rule<Model,Model> {
    List<? extends ModelAction> match(Model context);
}
