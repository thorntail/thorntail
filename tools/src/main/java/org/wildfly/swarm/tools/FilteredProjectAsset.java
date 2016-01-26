/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.tools;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.InputStream;

public abstract class FilteredProjectAsset implements ProjectAsset {
    private final ProjectAsset delegate;

    public FilteredProjectAsset(ProjectAsset delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getSimpleName() {
        return this.delegate.getSimpleName();
    }

    @Override
    public Archive<?> getArchive() {
        return filter(this.delegate.getArchive());
    }

    @Override
    public InputStream openStream() {
        return filter(ShrinkWrap.create(ZipImporter.class)
                              .importFrom(this.delegate.openStream())
                              .as(JavaArchive.class))
                .as(ZipExporter.class)
                .exportAsInputStream();
    }

    protected abstract Archive<?> filter(Archive<?> archive);
}
