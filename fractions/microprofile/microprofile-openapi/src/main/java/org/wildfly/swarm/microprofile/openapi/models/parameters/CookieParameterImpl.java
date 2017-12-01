/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.microprofile.openapi.models.parameters;

import org.eclipse.microprofile.openapi.models.parameters.CookieParameter;

/**
 * An implementation of the {@link CookieParameter} OpenAPI model interface.
 */
public class CookieParameterImpl extends ParameterImpl<CookieParameter> implements CookieParameter {

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getIn()
     */
    @Override
    public org.eclipse.microprofile.openapi.models.parameters.Parameter.In getIn() {
        return In.COOKIE;
    }

}
