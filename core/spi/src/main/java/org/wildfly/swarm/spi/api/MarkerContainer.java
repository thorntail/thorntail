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
package org.wildfly.swarm.spi.api;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.container.ManifestContainer;

/**
 * @author Ken Finnigan
 */
public interface MarkerContainer<T extends Archive<T>> extends ManifestContainer<T>, Archive<T> {
    ArchivePath PATH_MARKERS = ArchivePaths.create("markers");

    @SuppressWarnings("unchecked")
    default T addMarker(String markerName) throws Exception {
        addAsManifestResource(new StringAsset("marker"), ArchivePaths.create(PATH_MARKERS, markerName));

        return (T) this;
    }

    default boolean hasMarker(String markerName) throws Exception {
        return contains(ArchivePaths.create(PATH_MARKERS, markerName));
    }
}
