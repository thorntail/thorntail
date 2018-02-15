package org.jboss.unimbus.jca;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.jca.impl.JCAMessages;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class ResourceAdapterDeployments {

    public void addDeployment(ResourceAdapterDeployment deployment) {
        JCAMessages.MESSAGES.registeredDeployment(deployment.getUniqueId());
        this.deployments.add(deployment);
    }

    public List<ResourceAdapterDeployment> getDeployments() {
        return this.deployments;
    }

    private List<ResourceAdapterDeployment> deployments = new ArrayList<>();
}
