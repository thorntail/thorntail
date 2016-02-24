package org.wildfly.swarm.topology.openshift.runtime;

import com.openshift.restclient.ClientFactory;
import com.openshift.restclient.IClient;
import com.openshift.restclient.NoopSSLCertificateCallback;
import com.openshift.restclient.authorization.TokenAuthorizationStrategy;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.swarm.container.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientService implements Service<IClient> {

    public static final ServiceName SERVICE_NAME = OpenShiftTopologyConnector.SERVICE_NAME.append("client");

    private IClient client;

    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.client = openshiftClient();
            // Trigger an API call to ensure we fail-fast if we can't talk
            // to the API
            this.client.getCurrentUser().getName();
        } catch (IOException ex) {
            throw new StartException(ex);
        }
    }

    @Override
    public void stop(StopContext context) {
        this.client = null;
    }

    private IClient openshiftClient() throws IOException {
        String kubeHost = serviceHost("kubernetes");
        int kubePort = servicePort("kubernetes");

        Path tokenFile = Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token");
        String scheme = "http";
        String token = null;
        if (Files.exists(tokenFile)) {
            token = new String(Files.readAllBytes(tokenFile));
            scheme = "https";
        }

        IClient client = new ClientFactory().create(scheme + "://" + kubeHost + ":" + kubePort, new NoopSSLCertificateCallback());
        if (token != null) {
            client.setAuthorizationStrategy(new TokenAuthorizationStrategy(token));
        }

        return client;
    }

    @Override
    public IClient getValue() throws IllegalStateException, IllegalArgumentException {
        return this.client;
    }

    protected String serviceHost(String serviceName) {
        String envName = serviceName.replace("-", "_").toUpperCase() + "_SERVICE_HOST";
        return System.getenv(envName);
    }

    public static int servicePort(String serviceName) {
        String envName = serviceName.replace("-", "_").toUpperCase() + "_SERVICE_PORT";
        String envPort = System.getenv(envName);
        if (envPort == null) {
            return -1;
        }

        return Integer.parseInt(envPort);
    }
}
