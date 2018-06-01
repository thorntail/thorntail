/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.undertow.descriptors;

import java.io.IOException;

import io.undertow.servlet.ServletExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.api.container.ServiceProviderContainer;
import org.wildfly.swarm.spi.api.JBossDeploymentStructureContainer;
import org.wildfly.swarm.undertow.internal.FaviconServletExtension;

/**
 * Archive mix-in supporting manipulation of {@code web.xml}.
 *
 * @author Ken Finnigan
 */
public interface WebXmlContainer<T extends Archive<T>> extends Archive<T>, ServiceProviderContainer<T>, JBossDeploymentStructureContainer<T> {

    /**
     * Add a context parameter.
     *
     * @param name   The name of the context parameter.
     * @param values The values for the context parameter.
     * @return This archive.
     */
    @SuppressWarnings("unchecked")
    default T addContextParam(String name, String... values) {
        findWebXmlAsset().setContextParam(name, values);

        return (T) this;
    }

    /**
     * Retrieve the context parameter value.
     *
     * @param name The name of the context parameter.
     * @return Thie value of the context parameter.
     */
    default String getContextParamValue(String name) {
        return findWebXmlAsset().getContextParam(name);
    }

    /**
     * Add the default Thorntail {@code favicon.ico} handler.
     *
     * @return This archive.
     */
    @SuppressWarnings("unchecked")
    default T addFaviconExceptionHandler() throws IOException {
        // Add FaviconServletExtension
        String path = "WEB-INF/classes/" + FaviconServletExtension.EXTENSION_NAME.replace('.', '/') + ".class";
        byte[] generatedExtension;
        generatedExtension = FaviconFactory.createFaviconServletExtension(FaviconServletExtension.EXTENSION_NAME);
        add(new ByteArrayAsset(generatedExtension), path);

        // Add FaviconErrorHandler
        path = "WEB-INF/classes/" + FaviconServletExtension.HANDLER_NAME.replace('.', '/') + ".class";
        byte[] generatedHandler;
        generatedHandler = FaviconFactory.createFaviconErrorHandler(FaviconServletExtension.HANDLER_NAME);
        add(new ByteArrayAsset(generatedHandler), path);

        // Add services entry for FaviconServletExtension
        this.addAsServiceProvider(ServletExtension.class.getName(), FaviconServletExtension.EXTENSION_NAME);

        return (T) this;
    }

    /**
     * Add a servlet.
     *
     * @param servletName  The name of the servlet.
     * @param servletClass The class of the servlet.
     * @return This archive.
     */
    default Servlet addServlet(String servletName, String servletClass) {
        return findWebXmlAsset().addServlet(servletName, servletClass);
    }

    /**
     * Retrieve a servlet by class.
     *
     * @param servletClass The servlet class.
     * @return The servlet descriptor.
     */
    default Servlet servlet(String servletClass) {
        return findWebXmlAsset().getServlet(servletClass);
    }

    /**
     * Locate or create a {@code web.xml} asset.
     *
     * @return The existing or new {@code web.xml}.
     */
    default WebXmlAsset findWebXmlAsset() {
        final Node webXml = this.get(WebXmlAsset.NAME);
        NamedAsset asset;
        if (webXml == null) {
            asset = new WebXmlAsset();
            this.add(asset);
        } else {
            Asset tempAsset = webXml.getAsset();
            if (!(tempAsset instanceof WebXmlAsset)) {
                asset = new WebXmlAsset(tempAsset.openStream());
                this.add(asset);
            } else {
                asset = (NamedAsset) tempAsset;
            }
        }

        return (WebXmlAsset) asset;
    }
}
