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
package org.wildfly.swarm.resource.adapters;

import org.junit.Test;
import org.wildfly.swarm.config.resource.adapters.ResourceAdapter;
import org.wildfly.swarm.config.resource.adapters.ResourceAdapter.TransactionSupport;
import org.wildfly.swarm.config.resource.adapters.resource_adapter.AdminObjects;
import org.wildfly.swarm.config.resource.adapters.resource_adapter.ConfigProperties;
import org.wildfly.swarm.config.resource.adapters.resource_adapter.ConnectionDefinitions;
import org.wildfly.swarm.config.resource.adapters.resource_adapter.ConnectionDefinitions.FlushStrategy;

public class IronJacamarXmlAssetTest {
 
	@Test
	public void test() {		
		final ResourceAdapter<?> ra = new ResourceAdapter("key")
		    .archive("archive0")
		    .beanvalidationgroups("bean-validation-group0", "bean-validation-group1")
		    .bootstrapContext("bootstrap-context0")
			.configProperties(new ConfigProperties("name1").value("config-property0"))
			.transactionSupport(TransactionSupport.NOTRANSACTION)
			.connectionDefinitions(new ConnectionDefinitions("")
			     .useCcm(true)
			     .className("class-name1")
			     .jndiName("jndi-name1")
				 .enabled(true).useJavaContext(true)
				 .configProperties(new ConfigProperties("name5").value("config-property2"))
				 .configProperties(new ConfigProperties("name7").value("config-property3"))
				 .minPoolSize(50)
				 .maxPoolSize(50)
        		 .poolPrefill(false)
        		 .poolUseStrictMin(false)
        		 .flushStrategy(FlushStrategy.IDLECONNECTIONS)
        		 .securityDomain("security-domain0")
				 .blockingTimeoutWaitMillis(50L)
    			 .idleTimeoutMinutes(50L)
    			 .allocationRetry(50)
    			 .allocationRetryWaitMillis(50L)
    			 .xaResourceTimeout(50)
    			 .backgroundValidation(false)
    			 .backgroundValidationMillis(50L)
    			 .useFastFail(false)
    			 .noRecovery(false)
    			 .recoveryPluginClassName("class-name3")
    			 .recoveryPluginProperty("name9", "config-property4")
				 .useCcm(true))
             .adminObjects(new AdminObjects("")
				 .className("class-name9")
				 .jndiName("jndi-name5")
				 .enabled(true)
				 .useJavaContext(true)
				 .configProperties(new ConfigProperties("name17").value("config-property8"))
				 .configProperties(new ConfigProperties("name19").value("config-property9")))
		     .adminObjects(new AdminObjects("")
			     .className("class-nam21")
				 .jndiName("jndi-name7")
				 .enabled(true)
				 .useJavaContext(true)
				 .configProperties(new ConfigProperties("name21").value("config-propert10"))
				 .configProperties(new ConfigProperties("name23").value("config-property11"))
		);
		
		final String ironJacamarStr = IronJacamarXmlAssetImpl.INSTANCE.transform(ra);
		System.out.println(ironJacamarStr);
		
	}

}
