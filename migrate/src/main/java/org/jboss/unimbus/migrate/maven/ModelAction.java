package org.jboss.unimbus.migrate.maven;

import org.apache.maven.model.Model;
import org.jboss.unimbus.migrate.Action;

/**
 * Created by bob on 3/13/18.
 */
public interface ModelAction extends Action<Model, Model> {
    ModelRule getRule();
}
