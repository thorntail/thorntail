package io.thorntail.jca.impl.ironjacamar;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;
import org.jboss.jca.deployers.common.Configuration;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class DeployerConfiguration implements Configuration {

    @Override
    public void setBeanValidation(boolean value) {
        this.beanValidation = value;
    }

    @Override
    public boolean getBeanValidation() {
        return this.beanValidation;
    }

    @Override
    public void setArchiveValidation(boolean value) {
        this.archiveValidation = value;
    }

    @Override
    public boolean getArchiveValidation() {
        return this.archiveValidation;
    }

    @Override
    public void setArchiveValidationFailOnWarn(boolean value) {
        this.archiveValidationFailOnWarn = value;
    }

    @Override
    public boolean getArchiveValidationFailOnWarn() {
        return this.archiveValidationFailOnWarn;
    }

    @Override
    public void setArchiveValidationFailOnError(boolean value) {
        this.archiveValidationFailOnError = value;

    }

    @Override
    public boolean getArchiveValidationFailOnError() {
        return this.archiveValidationFailOnError;
    }

    @Override
    public void setDefaultBootstrapContext(CloneableBootstrapContext value) {
        this.defaultBootstrapContext = value;

    }

    @Override
    public CloneableBootstrapContext getDefaultBootstrapContext() {
        return this.defaultBootstrapContext;
    }

    @Override
    public void setBootstrapContexts(Map<String, CloneableBootstrapContext> value) {
        this.bootstrapContexts = value;

    }

    @Override
    public Map<String, CloneableBootstrapContext> getBootstrapContexts() {
        return this.bootstrapContexts;
    }

    @Inject
    @ConfigProperty(name = "jca.bean-validation.enabled")
    private boolean beanValidation;

    @Inject
    @ConfigProperty(name = "jca.archive-validation.enabled")
    private boolean archiveValidation;

    @Inject
    @ConfigProperty(name = "jca.archive-validation.fail-on-warn")
    private boolean archiveValidationFailOnWarn;

    @Inject
    @ConfigProperty(name = "jca.archive-validation.fail-on-error")
    private boolean archiveValidationFailOnError;

    @Inject
    private CloneableBootstrapContext defaultBootstrapContext;

    private Map<String, CloneableBootstrapContext> bootstrapContexts;
}
