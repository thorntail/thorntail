package io.thorntail.migrate;

import java.io.IOException;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.thorntail.Thorntail;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.migrate.maven.ModelRule;
import io.thorntail.migrate.config.ConfigRule;

/**
 * Created by bob on 3/12/18.
 */
@ApplicationScoped
public class Main {

    public static void main(String...args) throws Exception {
        Thorntail.run(Main.class);
    }

    void main(@Observes LifecycleEvent.Start event) throws IOException {
        System.err.println( "Running migration tool");

        new Migrator(Paths.get("."), modelRules, configRules).migrate();
    }

    @Inject
    @Any
    Instance<ModelRule> modelRules;

    @Inject
    @Any
    Instance<ConfigRule> configRules;
}
