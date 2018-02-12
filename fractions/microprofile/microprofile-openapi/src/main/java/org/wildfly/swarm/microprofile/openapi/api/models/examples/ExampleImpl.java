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

package org.wildfly.swarm.microprofile.openapi.api.models.examples;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;

/**
 * An implementation of the {@link Example} OpenAPI model interface.
 */
public class ExampleImpl extends ExtensibleImpl implements Example, ModelImpl {

    private String $ref;
    private String summary;
    private String description;
    private Object value;
    private String externalValue;

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#getRef()
     */
    @Override
    public String getRef() {
        return this.$ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_EXAMPLE + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(java.lang.String)
     */
    @Override
    public Example ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#getSummary()
     */
    @Override
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#setSummary(java.lang.String)
     */
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#summary(java.lang.String)
     */
    @Override
    public Example summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#description(java.lang.String)
     */
    @Override
    public Example description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#getValue()
     */
    @Override
    public Object getValue() {
        return this.value;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#value(java.lang.Object)
     */
    @Override
    public Example value(Object value) {
        this.value = value;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#getExternalValue()
     */
    @Override
    public String getExternalValue() {
        return this.externalValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#setExternalValue(java.lang.String)
     */
    @Override
    public void setExternalValue(String externalValue) {
        this.externalValue = externalValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.examples.Example#externalValue(java.lang.String)
     */
    @Override
    public Example externalValue(String externalValue) {
        this.externalValue = externalValue;
        return this;
    }

}