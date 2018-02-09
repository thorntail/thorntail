package org.jboss.unimbus.jms.artemis;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.common.metadata.spec.ConfigPropertyImpl;
import org.jboss.unimbus.condition.RequiredClassPresent;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.jca.ResourceAdapterDeploymentFactory;
import org.jboss.unimbus.jca.ResourceAdapterDeployments;
import org.jboss.unimbus.jca.ironjacamar.ResourceAdapterDeployment;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
@RequiredClassPresent("org.apache.activemq.artemis.ra.ActiveMQResourceAdapter")
public class ArtemisResourceAdapterDeployer {

    void init(@Observes LifecycleEvent.Scan event) throws Exception {
        ResourceAdapterDeployment deployment = factory.create("META-INF/artemis-ra.xml");
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

    ConfigProperty find(List<ConfigProperty> properties, String name) {
        return properties.stream()
                .filter(e -> e.getConfigPropertyName().getValue().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    void setProperty(List<ConfigProperty> properties, ConfigProperty original, String newValue) {
        ConfigPropertyImpl replacement = new ConfigPropertyImpl(
                original.getDescriptions(),
                original.getConfigPropertyName(),
                original.getConfigPropertyType(),
                new XsdString(newValue, original.getConfigPropertyName().getId()),
                original.getConfigPropertyIgnore(),
                original.getConfigPropertySupportsDynamicUpdates(),
                original.getConfigPropertyConfidential(),
                original.getId(),
                original.isMandatory(),
                original.getAttachedClassName(),
                original.getConfigPropertyIgnoreId(),
                original.getConfigPropertySupportsDynamicUpdatesId(),
                original.getConfigPropertyConfidentialId());

        properties.remove(original);
        properties.add(replacement);
    }

    @Inject
    ResourceAdapterDeployments deployments;

    @Inject
    ResourceAdapterDeploymentFactory factory;

    @Inject
    ArtemisClientConfiguration config;


}
