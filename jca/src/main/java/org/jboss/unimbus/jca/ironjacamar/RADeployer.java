package org.jboss.unimbus.jca.ironjacamar;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.resource.spi.ResourceAdapter;
import javax.transaction.TransactionManager;

import org.jboss.jca.common.api.metadata.common.SecurityMetadata;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.resourceadapter.WorkManager;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.metadata.resourceadapter.ActivationImpl;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.spi.mdr.AlreadyExistsException;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.deployers.DeployersLogger;
import org.jboss.jca.deployers.common.AbstractResourceAdapterDeployer;
import org.jboss.jca.deployers.common.CommonDeployment;
import org.jboss.jca.deployers.common.DeployException;
import org.jboss.logging.Logger;
import org.jboss.unimbus.UNimbus;

/**
 * Created by bob on 2/8/18.
 */
public class RADeployer extends AbstractResourceAdapterDeployer {

    public CommonDeployment deploy(ResourceAdapterDeployment resourceAdapterDeployment) throws Throwable {
        URL url = resourceAdapterDeployment.getRoot().toURI().toURL();
        String deploymentName = resourceAdapterDeployment.getUniqueId();
        File root = resourceAdapterDeployment.getRoot();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Connector connector = resourceAdapterDeployment.getConnector();
        Activation activation = activation();

        CommonDeployment deployment = createObjectsAndInjectValue(
                url,
                deploymentName,
                root,
                classLoader,
                connector,
                activation
        );

        return deployment;
    }

    public void setResourceAdapaterRepository(ResourceAdapterRepository resourceAdapaterRepository) {
        this.resourceAdapaterRepository = resourceAdapaterRepository;
    }

    public void setMetadataRepository(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    private Activation activation() {
        String id = "mything";
        String archive = "artemis.rar";
        TransactionSupportEnum transactionSupport = TransactionSupportEnum.NoTransaction;

        List<ConnectionDefinition> connectionDefinitions = new ArrayList<>();
        List<AdminObject> adminObjects = new ArrayList<>();
        Map<String, String> configProperties = new HashMap<>();
        List<String> beanValidationGroups = new ArrayList<>();
        String bootstrapContext = "default";
        WorkManager workmanager = null;
        ActivationImpl activation = new ActivationImpl(id,
                                                       archive,
                                                       transactionSupport,
                                                       connectionDefinitions,
                                                       adminObjects,
                                                       configProperties,
                                                       beanValidationGroups,
                                                       bootstrapContext,
                                                       workmanager);

        return activation;
    }

    public RADeployer() {
        super(true);
    }

    @Override
    protected SubjectFactory getSubjectFactory(SecurityMetadata securityMetadata, String jndiName) throws DeployException {
        return null;
    }

    @Override
    protected CachedConnectionManager getCachedConnectionManager() {
        return null;
    }

    @Override
    protected File getReportDirectory() {
        return null;
    }

    @Override
    protected void registerResourceAdapterToMDR(URL url, File root, Connector cmd, Activation activation) throws AlreadyExistsException {
        this.metadataRepository.registerResourceAdapter( url.getFile(), root, cmd, activation );

    }

    @Override
    protected String registerResourceAdapterToResourceAdapterRepository(ResourceAdapter instance) {
        return this.resourceAdapaterRepository.registerResourceAdapter(instance);
    }

    @Override
    protected void setRecoveryForResourceAdapterInResourceAdapterRepository(String key, boolean isXA) {

    }

    @Override
    protected TransactionManager getTransactionManager() {
        return null;
    }

    @Override
    protected TransactionIntegration getTransactionIntegration() {
        return null;
    }

    @Override
    protected PrintWriter getLogPrintWriter() {
        return null;
    }

    @Override
    protected String[] bindConnectionFactory(URL url, String deploymentName, Object cf) throws Throwable {
        return new String[0];
    }

    @Override
    protected String[] bindConnectionFactory(URL url, String deploymentName, Object cf, String jndiName) throws Throwable {
        return new String[0];
    }

    @Override
    protected String[] bindAdminObject(URL url, String deploymentName, Object ao) throws Throwable {
        return new String[0];
    }

    @Override
    protected String[] bindAdminObject(URL url, String deploymentName, Object ao, String jndiName) throws Throwable {
        return new String[0];
    }

    @Override
    protected boolean checkConfigurationIsValid() {
        return false;
    }

    @Override
    protected boolean checkActivation(Connector cmd, Activation activation) {
        return true;
    }

    @Override
    protected Object initAndInject(String className, List<? extends ConfigProperty> properties, ClassLoader cl) throws DeployException {
        try {
            Class<?> cls = cl.loadClass(className);
            Object instance = cls.newInstance();

            BeanInfo beanInfo = Introspector.getBeanInfo(cls);

            for (ConfigProperty property : properties) {
                String name = property.getConfigPropertyName().getValue();
                String value = property.getConfigPropertyValue().getValue();

                for (PropertyDescriptor each : beanInfo.getPropertyDescriptors()) {
                    if (each.getName().equalsIgnoreCase(name)) {
                        each.getWriteMethod().invoke(instance, value);
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            throw new DeployException(e.getMessage(), e);
        }

    }

    @Override
    protected DeployersLogger getLogger() {
        return Logger.getMessageLogger(DeployersLogger.class, UNimbus.loggerCategory("jca"));
    }

    private ResourceAdapterRepository resourceAdapaterRepository;
    private MetadataRepository metadataRepository;
}
