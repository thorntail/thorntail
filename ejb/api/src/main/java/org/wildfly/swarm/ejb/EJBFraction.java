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
package org.wildfly.swarm.ejb;

import org.wildfly.swarm.SwarmProperties;
import org.wildfly.swarm.config.EJB3;
import org.wildfly.swarm.config.ejb3.AsyncService;
import org.wildfly.swarm.config.ejb3.Cache;
import org.wildfly.swarm.config.ejb3.StrictMaxBeanInstancePool;
import org.wildfly.swarm.config.ejb3.ThreadPool;
import org.wildfly.swarm.config.ejb3.TimerService;
import org.wildfly.swarm.config.ejb3.service.FileDataStore;
import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.config.security.security_domain.ClassicAuthorization;
import org.wildfly.swarm.config.security.security_domain.authorization.PolicyModule;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.security.SecurityFraction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ken Finnigan
 * @author Lance Ball
 */
public class EJBFraction extends EJB3<EJBFraction> implements Fraction {

    protected EJBFraction() {

    }

    public static EJBFraction createDefaultFraction() {

        Map<Object,Object> threadPoolSettings = new HashMap<>();
        threadPoolSettings.put("time", "100");
        threadPoolSettings.put("unit", "MILLISECONDS");

        EJBFraction fraction = new EJBFraction();
        fraction.defaultStatefulBeanAccessTimeout(5000L)
                .defaultSingletonBeanAccessTimeout(5000L)
                .defaultSfsbCache("simple")
                .defaultSecurityDomain("other")
                .defaultMissingMethodPermissionsDenyAccess(true)
                .logSystemExceptions(true)
                .defaultResourceAdapterName(SwarmProperties.propertyVar("$ejb.resource-adapter-name", "activemq-ra.rar"))
                .strictMaxBeanInstancePool(new StrictMaxBeanInstancePool("slsb-strict-max-pool")
                        .maxPoolSize(20)
                        .timeout(5L)
                        .timeoutUnit(StrictMaxBeanInstancePool.TimeoutUnit.MINUTES))
                .strictMaxBeanInstancePool(new StrictMaxBeanInstancePool("mdb-strict-max-pool")
                        .maxPoolSize(20)
                        .timeout(5L)
                        .timeoutUnit(StrictMaxBeanInstancePool.TimeoutUnit.MINUTES))
                .cache(new Cache("simple"))
                .asyncService(new AsyncService().threadPoolName("default"))
                .timerService(new TimerService()
                        .threadPoolName("default")
                        .defaultDataStore("default-file-store")
                        .fileDataStore(new FileDataStore("default-file-store")
                                .path("timer-service-data")
                                .relativeTo("jboss.server.data.dir")))
                .threadPool(new ThreadPool("default")
                        .maxThreads(10)
                        .keepaliveTime(threadPoolSettings));

        return fraction;

    }

    @Override
    public void postInitialize(Container.PostInitContext initContext) {
        SecurityFraction security = (SecurityFraction) initContext.fraction("security");

        if (security != null) {
            SecurityDomain ejbPolicy = security.subresources().securityDomains().stream().filter((e) -> e.getKey().equals("jboss-ejb-policy")).findFirst().orElse(null);
            if (ejbPolicy == null) {
                ejbPolicy = new SecurityDomain("jboss-ejb-policy")
                        .classicAuthorization(new ClassicAuthorization()
                                .policyModule(new PolicyModule("default")
                                        .code("Delegating")
                                        .flag(Flag.REQUIRED)));
                security.securityDomain( ejbPolicy );
            }
        }

        System.err.println("found security: " + security);
    }
}
