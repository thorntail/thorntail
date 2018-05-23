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
package org.wildfly.swarm.odata.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.odata.ODataFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.teiid.TeiidFraction;

@Pre
@ApplicationScoped
public class ODataCustomizer implements Customizer {

    @Inject
    @Any
    TeiidFraction teiidFraction;

    @Inject
    @Any
    ODataFraction odataFraction;

    @Override
    public void customize() throws Exception {
        // when odata is secured through Keycloak, it uses "other" as security-domain.
        if (odataFraction.isSecure() && teiidFraction.authenticationSecurityDomain() == null) {
            teiidFraction.authenticationSecurityDomain("other");
        }
    }
}
