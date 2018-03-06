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

package org.wildfly.swarm.microprofile.openapi.api.models.responses;

import java.util.LinkedHashMap;

import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link APIResponses} OpenAPI model interface.
 */
public class APIResponsesImpl extends LinkedHashMap<String, APIResponse> implements APIResponses, ModelImpl {

    private static final long serialVersionUID = 7767651877116575739L;

    private APIResponse defaultValue;

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#addApiResponse(java.lang.String, org.eclipse.microprofile.openapi.models.responses.APIResponse)
     */
    @Override
    public APIResponses addApiResponse(String name, APIResponse apiResponse) {
        this.put(name, apiResponse);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#getDefault()
     */
    @Override
    public APIResponse getDefault() {
        return this.defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#setDefaultValue(org.eclipse.microprofile.openapi.models.responses.APIResponse)
     */
    @Override
    public void setDefaultValue(APIResponse defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponses#defaultValue(org.eclipse.microprofile.openapi.models.responses.APIResponse)
     */
    @Override
    public APIResponses defaultValue(APIResponse defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

}