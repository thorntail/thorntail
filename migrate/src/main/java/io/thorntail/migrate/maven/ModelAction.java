package io.thorntail.migrate.maven;

import org.apache.maven.model.Model;
import io.thorntail.migrate.Action;

/**
 * Created by bob on 3/13/18.
 */
public interface ModelAction extends Action<Model, Model> {
    ModelRule getRule();
}
