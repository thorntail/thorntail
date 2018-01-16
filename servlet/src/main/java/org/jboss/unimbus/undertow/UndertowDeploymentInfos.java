package org.jboss.unimbus.undertow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.undertow.servlet.api.DeploymentInfo;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class UndertowDeploymentInfos implements Iterable<DeploymentInfo> {

    public void add(DeploymentInfo deployment) {
        this.deployments.add( deployment );
    }

    public List<DeploymentInfo> getDeployments() {
        return this.deployments;
    }

    @Override
    public Iterator<DeploymentInfo> iterator() {
        return this.deployments.iterator();
    }

    private List<DeploymentInfo> deployments = new ArrayList<>();
}
