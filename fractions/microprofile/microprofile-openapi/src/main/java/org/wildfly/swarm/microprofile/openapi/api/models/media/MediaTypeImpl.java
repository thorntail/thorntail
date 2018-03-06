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

package org.wildfly.swarm.microprofile.openapi.api.models.media;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link Discriminator} MediaType model interface.
 */
public class MediaTypeImpl extends ExtensibleImpl implements MediaType, ModelImpl {

    private Schema schema;
    private Object example;
    private Map<String, Example> examples;
    private Map<String, Encoding> encoding;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#getSchema()
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#setSchema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#schema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public MediaType schema(Schema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return this.examples;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#setExamples(java.util.Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = examples;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#examples(java.util.Map)
     */
    @Override
    public MediaType examples(Map<String, Example> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#addExample(java.lang.String, org.eclipse.microprofile.openapi.models.examples.Example)
     */
    @Override
    public MediaType addExample(String key, Example example) {
        if (this.examples == null) {
            this.examples = new LinkedHashMap<>();
        }
        this.examples.put(key, example);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#setExample(java.lang.Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#example(java.lang.Object)
     */
    @Override
    public MediaType example(Object example) {
        this.example = example;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#getEncoding()
     */
    @Override
    public Map<String, Encoding> getEncoding() {
        return this.encoding;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#setEncoding(java.util.Map)
     */
    @Override
    public void setEncoding(Map<String, Encoding> encoding) {
        this.encoding = encoding;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#encoding(java.util.Map)
     */
    @Override
    public MediaType encoding(Map<String, Encoding> encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.MediaType#addEncoding(java.lang.String, org.eclipse.microprofile.openapi.models.media.Encoding)
     */
    @Override
    public MediaType addEncoding(String key, Encoding encodingItem) {
        if (this.encoding == null) {
            this.encoding = new LinkedHashMap<>();
        }
        this.encoding.put(key, encodingItem);
        return this;
    }

}