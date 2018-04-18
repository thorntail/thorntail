package io.thorntail.datasources.impl;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.thorntail.datasources.DataSourceMetaData;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Icon;
import org.jboss.jca.common.api.metadata.spec.LicenseType;
import org.jboss.jca.common.api.metadata.spec.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.common.metadata.spec.ConnectorImpl;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class ConnectorBuilder {

    Connector build(DataSourceMetaData ds) {
        XsdString moduleName = null;
        XsdString vendorName = null;
        XsdString eisType = null;
        XsdString resourceAdapterVersion = null;
        LicenseType license = null;
        ResourceAdapter resourceadapter = this.resourceAdapterBuilder.build(ds);
        List<XsdString> requiredWorkContexts = new ArrayList<>();
        boolean metadataComplete = true;
        List<LocalizedXsdString> description = new ArrayList<>();
        List<LocalizedXsdString> displayNames = new ArrayList<>();
        List<Icon> icons = new ArrayList<>();
        String id = ds.getId();
        ConnectorImpl connector = new ConnectorImpl(Connector.Version.V_17,
                                                    moduleName,
                                                    vendorName,
                                                    eisType,
                                                    resourceAdapterVersion,
                                                    license,
                                                    resourceadapter,
                                                    requiredWorkContexts,
                                                    metadataComplete,
                                                    description,
                                                    displayNames,
                                                    icons,
                                                    id);

        return connector;
    }

    @Inject
    ResourceAdapterBuilder resourceAdapterBuilder;
}
