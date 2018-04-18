package io.thorntail.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.thorntail.events.LifecycleEvent;

/**
 * Registry of all web deployments.
 *
 * <p>The contents of this registry may be altered and adjusted prior to the {@link LifecycleEvent.Deploy} event.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
@ApplicationScoped
public class Deployments implements Iterable<DeploymentMetaData> {

    @PostConstruct
    void configureList() {
        this.injectedDeployments.stream()
                .filter(Objects::nonNull)
                .forEach(this.deployments::add);
    }

    @Override
    public Iterator<DeploymentMetaData> iterator() {
        return this.deployments.iterator();
    }

    /**
     * Add a deployment.
     *
     * @param deployment The deployment descriptor.
     */
    public void addDeployment(DeploymentMetaData deployment) {
        if (deployment == null) {
            return;
        }
        this.deployments.add(deployment);
    }

    /**
     * Retrieve all deployments.
     *
     * @return The registered deployments.
     */
    public List getDeployments() {
        return this.deployments;
    }

    /**
     * Retrieve a stream of all deployments.
     *
     * @return The stream of all registered deployments.
     */
    public Stream<DeploymentMetaData> stream() {
        return this.deployments.stream();
    }

    private List<DeploymentMetaData> deployments = new ArrayList<>();

    @Inject
    private Instance<DeploymentMetaData> injectedDeployments;

}
