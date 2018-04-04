package org.jboss.unimbus.migrate;

import java.io.IOException;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.unimbus.UNimbus;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.migrate.config.ConfigRule;
import org.jboss.unimbus.migrate.maven.ModelRule;

/**
 * Created by bob on 3/12/18.
 */
@ApplicationScoped
public class Main {

    public static void main(String...args) throws Exception {
        UNimbus.run(Main.class);
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
