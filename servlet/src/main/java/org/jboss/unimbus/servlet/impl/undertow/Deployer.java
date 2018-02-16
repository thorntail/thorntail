package org.jboss.unimbus.servlet.impl.undertow;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletException;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.Deployments;
import org.jboss.unimbus.servlet.annotation.Management;
import org.jboss.unimbus.servlet.annotation.Primary;
import org.jboss.unimbus.servlet.impl.ServletMessages;
import org.jboss.unimbus.servlet.impl.undertow.metrics.MetricsIntegration;
import org.jboss.unimbus.servlet.impl.undertow.util.DeploymentUtils;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;

/**
 * Created by bob on 1/17/18.
 */
@ApplicationScoped
public class Deployer {

    void deploy(@Observes LifecycleEvent.Deploy event) {
        for (DeploymentMetaData each : this.deployments) {
            if (each == null) {
                continue;
            }
            PathHandler effectiveRoot = getEffectiveRoot(each);
            if (effectiveRoot == null) {
                continue;
            }

            DeploymentInfo info = DeploymentUtils.convert(each);
            info.addServletContextAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, this.beanManager);
            DeploymentManager manager = this.container.addDeployment(info);
            this.managers.add(manager);
            manager.deploy();

            try {
                HttpHandler handler = manager.start();
                handler = wrapForMetrics(info.getDeploymentName(), handler);
                effectiveRoot.addPrefixPath(info.getContextPath(), handler);
                this.handlers.add(handler);
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }
    }

    protected HttpHandler wrapForMetrics(String deploymentName, HttpHandler handler) {
        if (this.metricsIntegration.isUnsatisfied()) {
            return handler;
        }

        return this.metricsIntegration.get().integrate(deploymentName, handler);
    }

    String type(DeploymentMetaData meta) {
        if (this.selector.isUnified()) {
            return "unified";
        }

        if (this.selector.isManagementEnabled() && meta.isManagement()) {
            return "management";
        }

        return "primary";
    }

    void announce(@Observes LifecycleEvent.AfterStart event) {
        for (DeploymentMetaData each : this.deployments) {
            ServletMessages.MESSAGES.deployment(type(each), each.getName(), each.getContextPath());
        }
    }

    PathHandler getEffectiveRoot(DeploymentMetaData meta) {
        if (selector.isUnified()) {
            return this.publicRoot;
        }

        if (meta.isManagement()) {
            if (this.managementRoot != null) {
                return this.managementRoot;
            }
        }

        return this.publicRoot;
    }

    @Inject
    UndertowSelector selector;

    @Inject
    @Primary
    PathHandler publicRoot;

    @Inject
    @Management
    PathHandler managementRoot;

    @Inject
    ServletContainer container;

    @Inject
    Deployments deployments;

    private List<DeploymentManager> managers = new ArrayList<>();

    private List<HttpHandler> handlers = new ArrayList<>();

    @Inject
    BeanManager beanManager;

    @Inject
    Instance<MetricsIntegration> metricsIntegration;
}
