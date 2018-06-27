package io.thorntail.jca.impl.ironjacamar;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.resource.spi.ResourceAdapter;
import javax.transaction.TransactionManager;

import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.jboss.jca.common.api.metadata.common.SecurityMetadata;
import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.spi.mdr.AlreadyExistsException;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.rar.NotFoundException;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.deployers.DeployersLogger;
import org.jboss.jca.deployers.common.AbstractResourceAdapterDeployer;
import org.jboss.jca.deployers.common.CommonDeployment;
import org.jboss.jca.deployers.common.DeployException;
import org.jboss.logging.Logger;
import io.thorntail.jca.impl.JCAMessages;
import io.thorntail.jca.ResourceAdapterDeployment;
import io.thorntail.logging.impl.LoggingUtil;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class RADeployer extends AbstractResourceAdapterDeployer {

    @PostConstruct
    void init() {
        setConfiguration(this.config);
    }

    public CommonDeployment deploy(ResourceAdapterDeployment resourceAdapterDeployment) throws Throwable {
        URL url = resourceAdapterDeployment.getRoot().toURI().toURL();
        String deploymentName = resourceAdapterDeployment.getUniqueId();
        File root = resourceAdapterDeployment.getRoot();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Connector connector = resourceAdapterDeployment.getConnector();

        Activation activation = resourceAdapterDeployment.getActivation();

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

    @Override
    protected boolean requireExplicitJndiBindings() {
        return false;
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
        return this.cachedConnectionManager;
    }

    @Override
    protected File getReportDirectory() {
        return null;
    }

    @Override
    protected void registerResourceAdapterToMDR(URL url, File root, Connector cmd, Activation activation) throws AlreadyExistsException {
        this.metadataRepository.registerResourceAdapter(url.getFile(), root, cmd, activation);

    }

    @Override
    protected String registerResourceAdapterToResourceAdapterRepository(ResourceAdapter instance) {
        return this.resourceAdapaterRepository.registerResourceAdapter(instance);
    }

    @Override
    protected void setRecoveryForResourceAdapterInResourceAdapterRepository(String key, boolean isXA) {
        try {
            this.resourceAdapaterRepository.setRecoveryForResourceAdapter(key, isXA);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected TransactionManager getTransactionManager() {
        return this.transactionIntegration.getTransactionManager();
    }

    @Override
    protected TransactionIntegration getTransactionIntegration() {
        return this.transactionIntegration;
    }

    @Override
    protected PrintWriter getLogPrintWriter() {
        return null;
    }

    @Override
    protected String[] bindConnectionFactory(URL url, String deploymentName, Object cf) throws Throwable {
        String name = "java:jboss/" + deploymentName + "/connection-factory";
        this.jndi.bind(name, cf);
        JCAMessages.MESSAGES.bound(deploymentName, name);
        return new String[]{
                name
        };
    }

    @Override
    protected String[] bindConnectionFactory(URL url, String deploymentName, Object cf, String jndiName) throws Throwable {
        this.jndi.bind(jndiName, cf);
        JCAMessages.MESSAGES.bound(deploymentName, jndiName);
        return new String[]{
                jndiName
        };
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
        return true;
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
                        Object coerced = coerce(value, each.getWriteMethod().getParameterTypes()[0]);
                        each.getWriteMethod().invoke(instance, coerced);
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            throw new DeployException(e.getMessage(), e);
        }
    }

    private Object coerce(String value, Class<?> targetType) {
        return ((SmallRyeConfig) ConfigProviderResolver.instance().getConfig()).convert(value, targetType);
    }

    @Override
    protected DeployersLogger getLogger() {
        return Logger.getMessageLogger(DeployersLogger.class, LoggingUtil.loggerCategory("jca"));
    }

    @Inject
    private TransactionIntegration transactionIntegration;

    @Inject
    private ResourceAdapterRepository resourceAdapaterRepository;

    @Inject
    private MetadataRepository metadataRepository;

    @Inject
    private DeployerConfiguration config;

    @Inject
    private CachedConnectionManager cachedConnectionManager;

    @Inject
    private InitialContext jndi;
}
