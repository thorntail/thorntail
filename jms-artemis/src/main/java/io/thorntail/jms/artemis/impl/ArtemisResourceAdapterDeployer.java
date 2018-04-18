package io.thorntail.jms.artemis.impl;

import java.net.URL;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.TransactionManager;

import org.apache.activemq.artemis.service.extensions.ServiceUtils;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.jca.ResourceAdapterDeploymentFactory;
import io.thorntail.jca.ResourceAdapterDeployments;
import io.thorntail.jca.ResourceAdapterDeployment;

import static io.thorntail.jca.impl.Util.duplicateProperty;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
@RequiredClassPresent("org.apache.activemq.artemis.ra.ActiveMQResourceAdapter")
public class ArtemisResourceAdapterDeployer {

    void init(@Observes LifecycleEvent.Scan event) throws Exception {
        ServiceUtils.setTransactionManager(this.tm);
        ResourceAdapterDeployment deployment = this.factory.create("artemis", "META-INF/artemis-ra.xml");
        //System.err.println( "==========> " + deployment );
        if (deployment == null) {
            return;
        }

        ResourceAdapter ra = deployment.getConnector().getResourceadapter();
        if (ra != null) {
            List<ConfigProperty> properties = ra.getConfigProperties();
            ConfigProperty userName = find(properties, "UserName");
            if (!userName.isValueSet()) {
                if (this.config.getUsername() != null) {
                    setProperty(properties, userName, this.config.getUsername());
                }
            }
            ConfigProperty password = find(properties, "Password");
            if (!password.isValueSet()) {
                if (this.config.getPassword() != null) {
                    setProperty(properties, password, this.config.getPassword());
                }
            }
            ConfigProperty connectionParameters = find(properties, "ConnectionParameters");
            if (!connectionParameters.isValueSet()) {
                String str = "";
                if (this.config.getHost() != null) {
                    str += "host=" + this.config.getHost();
                    if (this.config.getPort() != null) {
                        if (!str.isEmpty()) {
                            str += ";";
                        }
                        str += "port=" + this.config.getPort();
                    }
                } else if (this.config.getUrl() != null) {
                    URL url = new URL(this.config.getUrl());
                    String host = url.getHost();
                    int port = url.getPort();
                    if (port == -1) {
                        port = 61616;
                    }
                    str = "host=" + host + ";port=" + port;
                }
                setProperty(properties, connectionParameters, str);
            }
        }
        this.deployments.addDeployment(deployment);

    }

    @PreDestroy
    void cleanup() {
        ServiceUtils.setTransactionManager(null);
    }

    ConfigProperty find(List<ConfigProperty> properties, String name) {
        return properties.stream()
                .filter(e -> e.getConfigPropertyName().getValue().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    void setProperty(List<ConfigProperty> properties, ConfigProperty original, String newValue) {
        ConfigProperty replacement = duplicateProperty(original, newValue);
        properties.remove(original);
        properties.add(replacement);
    }

    @Inject
    ResourceAdapterDeployments deployments;

    @Inject
    ResourceAdapterDeploymentFactory factory;

    @Inject
    ArtemisClientConfiguration config;

    @Inject
    TransactionManager tm;


}
