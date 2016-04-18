/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.resourceadapters;

import java.io.IOException;
import java.io.StringWriter;

import org.wildfly.swarm.config.resource.adapters.ResourceAdapter;
import org.wildfly.swarm.config.resource.adapters.resource_adapter.AdminObjects;
import org.wildfly.swarm.config.resource.adapters.resource_adapter.ConfigProperties;
import org.wildfly.swarm.config.resource.adapters.resource_adapter.ConnectionDefinitions;
import org.wildfly.swarm.container.util.XmlWriter;

/**
 * @author Ralf Battenfeld
 */
enum IronJacamarXmlAssetImpl {
    INSTANCE;
    
    public String transform(final ResourceAdapter<?> ra) {
        final StringWriter str = new StringWriter();
        try (XmlWriter out = new XmlWriter(str)) {
        	XmlWriter.Element ironJacamarElement = out.element("ironjacamar");
        	if (ra.beanvalidationgroups() != null && !ra.beanvalidationgroups().isEmpty()) {
	        	XmlWriter.Element beanValidationGroupsElement = null;
	        	for (final Object group : ra.beanvalidationgroups()) {
	        	    beanValidationGroupsElement = writeElement(ironJacamarElement, beanValidationGroupsElement, "bean-validation-groups", "bean-validation-group", group.toString());
	        	}
	        	beanValidationGroupsElement.end();
        	}

        	ironJacamarElement = writeElement(null, ironJacamarElement, "ironjacamar", "bootstrap-context", ra.bootstrapContext());
        	for (final ConfigProperties<?> prop : ra.subresources().configProperties()) {
        		ironJacamarElement = writeConfigProperty(null, ironJacamarElement, "ironjacamar", prop);
    	    }
        	ironJacamarElement = writeElement(null, ironJacamarElement, "ironjacamar", "transaction-support", ra.transactionSupport());

            if (ra.subresources().connectionDefinitions() != null && !ra.subresources().connectionDefinitions().isEmpty()) {
            	final XmlWriter.Element connDefsElement = ironJacamarElement.element("connection-definitions");
	            for (final ConnectionDefinitions<?> connDef : ra.subresources().connectionDefinitions()) {
	            	XmlWriter.Element connDefElement = null;
	            	connDefElement = writeAttribute(connDefsElement, connDefElement, "connection-definition", "use-ccm", connDef.useCcm());
	            	connDefElement = writeAttribute(connDefsElement, connDefElement, "connection-definition", "class-name", connDef.className());
	            	connDefElement = writeAttribute(connDefsElement, connDefElement, "connection-definition", "jndi-ccm", connDef.jndiName());
	            	connDefElement = writeAttribute(connDefsElement, connDefElement, "connection-definition", "enabled", connDef.enabled());
	            	connDefElement = writeAttribute(connDefsElement, connDefElement, "connection-definition", "use-java-context", connDef.useJavaContext());

                    if (connDef.subresources().configProperties() != null && !connDef.subresources().configProperties().isEmpty()) {
		            	for (final ConfigProperties<?> prop : connDef.subresources().configProperties()) {
		            		connDefElement = writeConfigProperty(connDefsElement, connDefElement, "connection-definition", prop);
		        	    }
                    }

	            	XmlWriter.Element poolElement = null;
	            	poolElement = writeElement(connDefElement, poolElement, "pool", "min-pool-size", connDef.minPoolSize());
	            	poolElement = writeElement(connDefElement, poolElement, "pool", "max-pool-size", connDef.maxPoolSize());
	            	poolElement = writeElement(connDefElement, poolElement, "pool", "prefill", connDef.poolPrefill());
	            	poolElement = writeElement(connDefElement, poolElement, "pool", "use-strict-min", connDef.poolUseStrictMin());
	            	poolElement = writeElement(connDefElement, poolElement, "pool", "flush-strategy", connDef.flushStrategy());
	            	if (poolElement != null) {
	                    poolElement.end();
	            	}
	            	
	            	XmlWriter.Element securityElement = null;
	            	securityElement = writeElement(connDefElement, securityElement, "security", "security-domain", connDef.securityDomain());
	            	securityElement = writeElement(connDefElement, securityElement, "security", "security-domain-and-application", connDef.securityDomainAndApplication());
	            	if (securityElement != null) {
	            		securityElement.end();
	            	}

	            	XmlWriter.Element timeoutElement = null;	            	
	            	timeoutElement = writeElement(connDefsElement, timeoutElement, "timeout", "blocking-timeout-millis", connDef.blockingTimeoutWaitMillis());	 // TODO check why not blockingTimeoutMillis())         	
	            	timeoutElement = writeElement(connDefsElement, timeoutElement, "timeout", "idle-timeout-minutes", connDef.idleTimeoutMinutes());	            	
	            	timeoutElement = writeElement(connDefsElement, timeoutElement, "timeout", "allocation-retry", connDef.allocationRetry());	            	
	            	timeoutElement = writeElement(connDefsElement, timeoutElement, "timeout", "allocation-retry-wait-millis", connDef.allocationRetryWaitMillis());	            	
	            	timeoutElement = writeElement(connDefsElement, timeoutElement, "timeout", "xa-resource-timeout", connDef.xaResourceTimeout());
	            	if (timeoutElement != null) {
	            		timeoutElement.end();
	            	}

	            	XmlWriter.Element validationElement = null;	            	
	            	validationElement = writeElement(connDefsElement, validationElement, "validation", "background-validation", connDef.backgroundValidation());
	            	validationElement = writeElement(connDefsElement, validationElement, "validation", "background-validation-millis", connDef.backgroundValidationMillis());   
	            	validationElement = writeElement(connDefsElement, validationElement, "validation", "use-fast-fail", connDef.useFastFail());         	
	            	if (validationElement != null) {
	            		validationElement.end();
	            	}

	            	XmlWriter.Element recoveryElement = null;
	            	XmlWriter.Element recoverCredentialElement = null;
	            	XmlWriter.Element recoveryPluginElement = null;
	            	if (connDef.recoveryUsername() != null || connDef.recoveryPassword() != null || connDef.recoverySecurityDomain() != null) {
	            		recoveryElement = connDefsElement.element("recovery");
	            		recoveryElement = writeAttribute(connDefsElement, recoveryElement, "recovery", "no-recovery", connDef.noRecovery());
	            		recoverCredentialElement = writeElement(recoveryElement, recoverCredentialElement, "recover-credential", "user-name", connDef.recoveryUsername());
	            		recoverCredentialElement = writeElement(recoveryElement, recoverCredentialElement, "recover-credential", "password", connDef.recoveryPassword());
	            		recoverCredentialElement = writeElement(recoveryElement, recoverCredentialElement, "recover-credential", "security-domain", connDef.recoverySecurityDomain());
		            	if (recoverCredentialElement != null) {
		            		recoverCredentialElement.end();
		            	}
	            	}
	            	if (connDef.recoveryPluginClassName() != null || (connDef.recoveryPluginProperties() != null && !connDef.recoveryPluginProperties().isEmpty())) {
	            	    if (recoveryElement == null) {
	            	    	recoveryElement = connDefsElement.element("recovery");
	            	    	recoveryElement = writeAttribute(connDefsElement, recoveryElement, "recovery", "no-recovery", connDef.noRecovery());
	            	    }
	            	    recoveryPluginElement = writeAttribute(recoveryElement, recoveryPluginElement, "recover-plugin", "class-name", connDef.recoveryPluginClassName());
	            	    recoveryPluginElement = writeElement(recoveryElement, recoveryPluginElement, "recover-plugin", "user-name", connDef.recoveryUsername());
	            	    for (Object key : connDef.recoveryPluginProperties().keySet()) {
	            	    	recoveryPluginElement.element("config-property").attr("name", key.toString()).content(connDef.recoveryPluginProperties().get(key).toString()).end();
	            	    }		            
	            	    if (recoveryPluginElement != null) {
	            	    	recoveryPluginElement.end();
		            	}
	            	}
	            	if (recoveryElement != null) {
	            		recoveryElement.end();
	            	}
	            	connDefElement.end();
	            }
	            connDefsElement.end();
            }
            
            if (ra.subresources().adminObjects() != null && !ra.subresources().adminObjects().isEmpty()) {
                final XmlWriter.Element adminObjsElement = ironJacamarElement.element("admin-objects");            	
            	for (final AdminObjects<?> adminObject : ra.subresources().adminObjects()) {
            		XmlWriter.Element adminObjElement = null;
            		adminObjElement = writeAttribute(adminObjsElement, adminObjElement, "admin-object", "class-name", adminObject.className());
            		adminObjElement = writeAttribute(adminObjsElement, adminObjElement, "admin-object", "jndi-name", adminObject.jndiName());
            		adminObjElement = writeAttribute(adminObjsElement, adminObjElement, "admin-object", "enabled", adminObject.enabled());
            		adminObjElement = writeAttribute(adminObjsElement, adminObjElement, "admin-object", "use-java-context", adminObject.useJavaContext());
            		for (final ConfigProperties<?> prop : adminObject.subresources().configProperties()) {
                        adminObjElement = writeConfigProperty(adminObjsElement, adminObjElement, "admin-object", prop);
            	    }
            		if (adminObjElement != null) {
            			adminObjElement.end();
	            	}
            	}
            	adminObjsElement.end();
            }
            
            ironJacamarElement.end();
            out.close();
            return str.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //-----------------------------------------------------------------------||
    //-- Private Methods ----------------------------------------------------||
    //-----------------------------------------------------------------------||

    private XmlWriter.Element writeElement(final XmlWriter.Element parentParentElement, final XmlWriter.Element parentElement, final String parentElementName, final String name, final Object value) throws IOException {
    	XmlWriter.Element localParentElement = parentElement;
    	if (value != null) {
    		if (localParentElement == null) {
    			localParentElement = parentParentElement.element(parentElementName);
    		}
    		localParentElement.element(name).content(value.toString()).end();
    	}
    	return localParentElement;
    }
    
    private XmlWriter.Element writeAttribute(final XmlWriter.Element parentParentElement, final XmlWriter.Element parentElement, final String parentElementName, final String name, final Object value) throws IOException {
    	XmlWriter.Element localParentElement = parentElement;
    	if (value != null) {
    		if (localParentElement == null) {
    			localParentElement = parentParentElement.element(parentElementName);
    		}
    		localParentElement.attr(name, value.toString());
    	}
    	return localParentElement;
    }

    private XmlWriter.Element writeConfigProperty(final XmlWriter.Element parentParentElement, final XmlWriter.Element parentElement, final String parentElementName, final ConfigProperties<?> prop) throws IOException {
    	XmlWriter.Element localParentElement = parentElement;
    	if (prop != null) {
    		if (localParentElement == null) {
    			localParentElement = parentParentElement.element(parentElementName);
    		}
    		localParentElement.element("config-property").attr("name", prop.getKey()).content(prop.value()).end();
    	}
    	return localParentElement;
    }
    
}
