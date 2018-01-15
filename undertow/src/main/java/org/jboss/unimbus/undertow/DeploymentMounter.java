package org.jboss.unimbus.undertow;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletException;

import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import org.jboss.unimbus.events.BeforeStart;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class DeploymentMounter {

    void mount(@Observes @BeforeStart Boolean event) {
        for (DeploymentManager manager : managers) {
            manager.deploy();
            try {
                root.addPrefixPath( "/", manager.start() );
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }
    }

    @Inject
    PathHandler root;

    @Inject
    List<DeploymentManager> managers;
}
