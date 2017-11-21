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
package org.wildfly.swarm.mpjwtauth.detect;

import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon31.LoginConfigType;
import org.wildfly.swarm.spi.meta.WebXmlFractionDetector;

/**
 * A detector that looks at the WebXml descriptor for a login-config/auth-method of type MP-JWT.
 */
public class MPJWTAuthWebXmlDetector extends WebXmlFractionDetector {

    @Override
    public String artifactId() {
        return "org/wildfly/swarm/mpjwtauth";
    }

    /**
     * @return true of the WebXml indicates an MP-JWT auth-method
     */
    @Override
    protected boolean doDetect() {
        super.doDetect();
        boolean isMPJWTAuth = false;
        if (webXMl.getAllLoginConfig().size() > 0) {
            LoginConfigType<WebAppDescriptor> lc = webXMl.getOrCreateLoginConfig();
            isMPJWTAuth = lc.getAuthMethod() != null && lc.getAuthMethod().equalsIgnoreCase("MP-JWT");
        }
        return isMPJWTAuth;
    }
}
