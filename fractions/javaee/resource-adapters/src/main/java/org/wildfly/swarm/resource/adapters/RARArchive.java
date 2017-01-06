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
package org.wildfly.swarm.resource.adapters;

import java.io.File;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ManifestContainer;
import org.jboss.shrinkwrap.api.container.ResourceAdapterContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.wildfly.swarm.config.resource.adapters.ResourceAdapter;
import org.wildfly.swarm.config.resource.adapters.ResourceAdapterConsumer;

/** A resource-adapter archive.
 *
 * <p>This archive provides a way to deploy a resource-adapter similar to the
 * Fraction-level configuration of a resource-adapter.</p>
 *
 * @see ResourceAdapterFraction
 *
 * @author Ralf Battenfeld
 */
public interface RARArchive extends Archive<RARArchive>, ManifestContainer<RARArchive>, LibraryContainer<RARArchive>, ResourceContainer<RARArchive>, ResourceAdapterContainer<RARArchive> {

    /** Add and configure a resource-adapter.
     *
     * @param key The key of the resource-adapter.
     * @param consumer The configuring consumer of the resource-adapter.
     * @return This archive.
     */
    RARArchive resourceAdapter(String key, ResourceAdapterConsumer consumer);

    /** Add a fully-configured resource-adapter.
     *
     * @param ra The configured resource-adapter.
     * @return This archive.
     */
    RARArchive resourceAdapter(ResourceAdapter ra);

    /** Add an IronJacamar file.
     *
     * @param ironjacamarFile The IronJacamar file.
     * @return This archive.
     */
    RARArchive resourceAdapter(File ironjacamarFile);

    /** Add an IronJacamar asset.
     *
     * @param ironjacamarAsset The IronJacamar asset.
     * @return This archive.
     */
    RARArchive resourceAdapter(Asset ironjacamarAsset);
}