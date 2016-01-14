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
package org.wildfly.swarm.arquillian.resources;

import org.jboss.arquillian.test.api.ArquillianResource;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class SwarmURIResourceProvider extends SwarmURLResourceProvider {
    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {

        Object object = super.lookup(resource, qualifiers);
        if (object == null) {
            return null;
        }
        try {
            return ((URL) object).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canProvide(Class<?> type) {
        return URI.class.isAssignableFrom(type);
    }
}
