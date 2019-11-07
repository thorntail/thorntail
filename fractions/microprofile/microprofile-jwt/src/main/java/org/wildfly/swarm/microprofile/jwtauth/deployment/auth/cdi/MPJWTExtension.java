/**
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.JWTAuthMechanism;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.config.JWTAuthContextInfoProvider;

import io.smallrye.jwt.auth.cdi.ClaimValueProducer;
import io.smallrye.jwt.auth.cdi.CommonJwtProducer;
import io.smallrye.jwt.auth.cdi.JsonValueProducer;
import io.smallrye.jwt.auth.cdi.PrincipalProducer;
import io.smallrye.jwt.auth.cdi.RawClaimTypeProducer;

/**
 * A CDI extension that provides a producer for the current authenticated JsonWebToken based on a thread
 * local value that is managed by the {@link JWTAuthMechanism} request
 * authentication handler.
 *
 * This also installs the producer methods for the discovered:
 * <ul>
 * <li>@Claim ClaimValue<T> injection sites.</li>
 * <li>@Claim raw type<T> injection sites.</li>
 * <li>@Claim JsonValue injection sites.</li>
 * </ul>
 *
 * @see JWTAuthMechanism
 */
public class MPJWTExtension extends ProviderExtensionSupport implements Extension {
    private static Logger log = Logger.getLogger(MPJWTExtension.class);

    /**
     * Register the MPJWTProducer JsonWebToken producer bean
     *
     * @param bbd         before discovery event
     * @param beanManager cdi bean manager
     */
    public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager beanManager) {
        log.debugf("MPJWTExtension(), adding producers");
        String extensionName = MPJWTExtension.class.getName();
        for (Class<?> clazz : new Class<?>[] {
                JWTAuthContextInfoProvider.class,
                CommonJwtProducer.class,
                PrincipalProducer.class,
                RawClaimTypeProducer.class,
                ClaimValueProducer.class,
                JsonValueProducer.class,
        }) {
            bbd.addAnnotatedType(beanManager.createAnnotatedType(clazz), extensionName + "_" + clazz.getName());
        }
    }
}
