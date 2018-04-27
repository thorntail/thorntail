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

package io.thorntail.openapi.impl.api.models.examples;

import io.thorntail.openapi.impl.OpenApiConstants;
import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import io.thorntail.openapi.impl.api.models.ModelImpl;
import org.eclipse.microprofile.openapi.models.examples.Example;

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
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_EXAMPLE + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(String)
     */
    @Override
    public Example ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see Example#getSummary()
     */
    @Override
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see Example#setSummary(String)
     */
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @see Example#summary(String)
     */
    @Override
    public Example summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * @see Example#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see Example#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Example#description(String)
     */
    @Override
    public Example description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see Example#getValue()
     */
    @Override
    public Object getValue() {
        return this.value;
    }

    /**
     * @see Example#setValue(Object)
     */
    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @see Example#value(Object)
     */
    @Override
    public Example value(Object value) {
        this.value = value;
        return this;
    }

    /**
     * @see Example#getExternalValue()
     */
    @Override
    public String getExternalValue() {
        return this.externalValue;
    }

    /**
     * @see Example#setExternalValue(String)
     */
    @Override
    public void setExternalValue(String externalValue) {
        this.externalValue = externalValue;
    }

    /**
     * @see Example#externalValue(String)
     */
    @Override
    public Example externalValue(String externalValue) {
        this.externalValue = externalValue;
        return this;
    }

}