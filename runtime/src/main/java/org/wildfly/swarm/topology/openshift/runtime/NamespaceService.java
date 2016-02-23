package org.wildfly.swarm.topology.openshift.runtime;

import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IProject;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.util.List;

public class NamespaceService implements Service<String> {

    public static final ServiceName SERVICE_NAME = OpenShiftTopologyConnector.SERVICE_NAME.append("namespace");

    private InjectedValue<IClient> clientInjector = new InjectedValue<>();

    private IClient client;

    private String namespace;

    public Injector<IClient> getClientInjector() {
        return this.clientInjector;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.client = this.clientInjector.getValue();

        this.namespace = System.getenv("KUBERNETES_NAMESPACE");

        if (this.namespace == null) {
            this.namespace = System.getenv("OPENSHIFT_BUILD_NAMESPACE");
        }

        if (this.namespace == null) {
            List<IProject> projects = this.client.list(ResourceKind.PROJECT);
            if (projects.size() != 1) {
                throw new StartException("Unable to automatically detect the " +
                        "Kubernetes namespace to use. Set the environment " +
                        "variable KUBERNETES_NAMESPACE and try again.");
            }
            this.namespace = projects.get(0).getNamespace();
        }
    }

    @Override
    public void stop(StopContext context) {
        this.client = null;
        this.namespace = null;
    }

    @Override
    public String getValue() throws IllegalStateException, IllegalArgumentException {
        return this.namespace;
    }
}
