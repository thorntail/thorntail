package io.thorntail.migrate.maven;

import java.util.List;

import io.thorntail.migrate.Rule;
import org.apache.maven.model.Model;

/**
 * Created by bob on 3/13/18.
 */
public interface ModelRule extends Rule<Model,Model> {
    List<? extends ModelAction> match(Model context);
}
