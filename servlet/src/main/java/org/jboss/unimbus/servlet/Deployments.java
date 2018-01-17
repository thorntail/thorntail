package org.jboss.unimbus.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by bob on 1/17/18.
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

    public void addDeployment(DeploymentMetaData meta) {
        if ( meta == null ) {
            return;
        }
        this.deployments.add(meta);
    }

    public List getDeployments() {
        return Collections.unmodifiableList(this.deployments);
    }

    private List<DeploymentMetaData> deployments = new ArrayList<>();

    @Inject
    private Instance<DeploymentMetaData> injectedDeployments;

}
