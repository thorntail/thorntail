package io.thorntail.datasources.impl;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.jca.common.api.metadata.common.Pool;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.Security;
import org.jboss.jca.common.api.metadata.common.TimeOut;
import org.jboss.jca.common.api.metadata.common.Validation;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.metadata.resourceadapter.ConnectionDefinitionImpl;
import io.thorntail.datasources.DataSourceMetaData;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class NonXAConnectionDefinitionBuilder {

    ConnectionDefinition build(DataSourceMetaData ds) {
        Map<String, String> configProperties = new HashMap<>();
        String className = null;
        String jndiName = ds.getJNDIName();
        String poolName = null;
        Boolean enabled = true;
        Boolean useJavaContext = null;
        Boolean useCcm = true;
        Boolean sharable = true;
        Boolean enlistment = true;
        Boolean connectable = true;
        Boolean tracking = false;
        String mcp = null;
        Boolean enlistmentTrace = null;
        Pool pool = null;
        TimeOut timeOut = null;
        Validation validation = null;
        Security security = null;
        Recovery recovery = null;
        Boolean isXA = null;

        ConnectionDefinitionImpl cd = new ConnectionDefinitionImpl(
                configProperties,
                className,
                jndiName,
                poolName,
                enabled,
                useJavaContext,
                useCcm,
                sharable,
                enlistment,
                connectable,
                tracking,
                mcp,
                enlistmentTrace,
                pool,
                timeOut,
                validation,
                security,
                recovery,
                isXA);


        return cd;
    }
}
