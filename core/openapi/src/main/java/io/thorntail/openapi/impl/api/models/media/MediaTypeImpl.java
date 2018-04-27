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

package io.thorntail.openapi.impl.api.models.media;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import io.thorntail.openapi.impl.api.models.ModelImpl;

/**
 * An implementation of the {@link Discriminator} MediaType model interface.
 */
public class MediaTypeImpl extends ExtensibleImpl implements MediaType, ModelImpl {

    private Schema schema;

    private Object example;

    private Map<String, Example> examples;

    private Map<String, Encoding> encoding;

    /**
     * @see MediaType#getSchema()
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }

    /**
     * @see MediaType#setSchema(Schema)
     */
    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * @see MediaType#schema(Schema)
     */
    @Override
    public MediaType schema(Schema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * @see MediaType#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return this.examples;
    }

    /**
     * @see MediaType#setExamples(Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = examples;
    }

    /**
     * @see MediaType#examples(Map)
     */
    @Override
    public MediaType examples(Map<String, Example> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * @see MediaType#addExample(String, Example)
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
     * @see MediaType#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see MediaType#setExample(Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see MediaType#example(Object)
     */
    @Override
    public MediaType example(Object example) {
        this.example = example;
        return this;
    }

    /**
     * @see MediaType#getEncoding()
     */
    @Override
    public Map<String, Encoding> getEncoding() {
        return this.encoding;
    }

    /**
     * @see MediaType#setEncoding(Map)
     */
    @Override
    public void setEncoding(Map<String, Encoding> encoding) {
        this.encoding = encoding;
    }

    /**
     * @see MediaType#encoding(Map)
     */
    @Override
    public MediaType encoding(Map<String, Encoding> encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * @see MediaType#addEncoding(String, Encoding)
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