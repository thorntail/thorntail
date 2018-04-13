package org.jboss.unimbus.websockets.impl.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.undertow.servlet.api.Deployment;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.Deployments;
import org.jboss.unimbus.websockets.WebSocketMetaData;
import org.jboss.unimbus.websockets.ext.WebSocketsExtension;

/**
 * Created by bob on 4/13/18.
 */
@ApplicationScoped
public class WebSocketsDeploymentsCustomizer {

    void customize(@Observes LifecycleEvent.Initialize event) {
        DeploymentMetaData deployment = getTargetDeployment();
        WebSocketDeploymentInfo info = new WebSocketDeploymentInfo();
        for (WebSocketMetaData each : this.ext.getMetaData()) {
            info.addEndpoint(each.getEndpoint());
        }

        deployment.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME,
                                              info);
    }

    DeploymentMetaData getTargetDeployment() {
        DeploymentMetaData target = null;
        for (DeploymentMetaData deployment : this.deployments) {
            if (!deployment.isManagement()) {
                target = deployment;
                break;
            }

        }

        if (target == null) {
            target = new DeploymentMetaData("websockets");
            target.setContextPath("/");
            this.deployments.addDeployment(target);
        }

        return target;
    }

    @Inject
    Deployments deployments;

    @Inject
    WebSocketsExtension ext;
}
