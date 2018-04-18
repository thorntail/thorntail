package io.thorntail.datasources.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.resourceadapter.WorkManager;
import org.jboss.jca.common.metadata.resourceadapter.ActivationImpl;
import io.thorntail.datasources.DataSourceMetaData;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class ActivationBuilder {

    Activation build(DataSourceMetaData ds) {
        String id = ds.getId();
        String archive = ds.getId();
        TransactionSupportEnum transactionSupport = TransactionSupportEnum.LocalTransaction;
        List<ConnectionDefinition> connectionDefinitions = new ArrayList<>();
        connectionDefinitions.add( this.nonXaBuilder.build(ds));
        List<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject> adminObjects = new ArrayList<>();
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


    @Inject
    NonXAConnectionDefinitionBuilder nonXaBuilder;
}
