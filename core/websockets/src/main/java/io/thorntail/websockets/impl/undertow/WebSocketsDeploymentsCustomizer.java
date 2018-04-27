package io.thorntail.websockets.impl.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.thorntail.events.LifecycleEvent;
import io.thorntail.servlet.DeploymentMetaData;
import io.thorntail.websockets.WebSocketMetaData;
import io.thorntail.websockets.ext.WebSocketsExtension;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import io.thorntail.servlet.Deployments;

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
