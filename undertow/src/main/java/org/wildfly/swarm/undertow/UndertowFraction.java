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
package org.wildfly.swarm.undertow;

import java.util.List;

import javax.annotation.PostConstruct;

import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.undertow.BufferCache;
import org.wildfly.swarm.config.undertow.HandlerConfiguration;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.ServletContainer;
import org.wildfly.swarm.config.undertow.server.Host;
import org.wildfly.swarm.config.undertow.servlet_container.JSPSetting;
import org.wildfly.swarm.config.undertow.servlet_container.WebsocketsSetting;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.annotations.Default;
import org.wildfly.swarm.spi.api.annotations.ExtensionModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;

/**
 * @author Bob McWhirter
 */
@ExtensionModule("org.wildfly.extension.undertow")
@MarshalDMR
public class UndertowFraction extends Undertow<UndertowFraction> implements Fraction {

    public UndertowFraction() {
    }

    /**
     * Create the default, HTTP-only fraction.
     *
     * @return The configured fraction.
     */
    @Default
    public static UndertowFraction createDefaultFraction() {
        UndertowFraction fraction = new UndertowFraction();
        return fraction.applyDefaults();
    }

    @PostConstruct
    public UndertowFraction applyDefaults() {
        final boolean enabled = (System.getProperty(SwarmProperties.HTTP_EAGER) != null);

        server(new Server("default-server")
                .httpListener("default", (listener) -> {
                    listener.socketBinding("http")
                            .enabled(enabled);
                })
                .host(new Host("default-host")))
                .bufferCache(new BufferCache("default"))
                .servletContainer(new ServletContainer("default")
                        .websocketsSetting(new WebsocketsSetting())
                        .jspSetting(new JSPSetting()))
                .handlerConfiguration(new HandlerConfiguration());

        return this;
    }

    /**
     * Create the default HTTP and HTTPS fraction.
     *
     * <p>This default requires configuration for accessing a keystore.
     * The application also <b>must</b> include the <code>management</code>
     * fraction in its dependencies.</p>
     *
     * @param path     The keystore path.
     * @param password The keystore password.
     * @param alias    The server certificate alias.
     * @return The configured fraction.
     * @see #enableHTTPS(String, String, String)
     */
    public static UndertowFraction createDefaultFraction(String path, String password, String alias) {
        return createDefaultFraction()
                .enableHTTPS(path, password, alias);
    }

    /**
     * Create the default HTTP and AJP fraction.
     *
     * @return The configured fraction.
     * @see #enableAJP()
     */
    public static UndertowFraction createDefaultAndEnableAJPFraction() {
        return createDefaultFraction()
                .enableAJP();
    }

    /**
     * Create the default HTTPS-only fraction.
     *
     * <p>This default inhibits the non-SSL HTTP endpoint, and only creates
     * the default HTTPS endpoint. The application also <b>must</b> include
     * the <code>management</code> fraction in its dependencies.</p>
     *
     * @param path     The keystore path.
     * @param password The keystore password.
     * @param alias    The server certificate alias.
     * @return The configured fraction;
     * @see #enableHTTPS(String, String, String)
     */
    public static UndertowFraction createDefaultHTTPSOnlyFraction(String path, String password, String alias) {
        UndertowFraction fraction = createDefaultFraction();
        fraction.removeHttpListenersFromDefaultServer()
                .enableHTTPS(path, password, alias);
        return fraction;
    }

    /**
     * Create the default AJP-only fraction.
     *
     * <p>This default inhibits the HTTP endpoint, and only creates
     * the default AJP endpoint.</p>
     *
     * @return The configured fraction.
     * @see #enableAJP()
     */
    public static UndertowFraction createDefaultAJPOnlyFraction() {
        UndertowFraction fraction = createDefaultFraction();
        fraction.removeHttpListenersFromDefaultServer()
                .enableAJP();
        return fraction;
    }

    /**
     * Enable HTTPS on this fraction.
     *
     * <p>This will enable HTTPS of the fraction. The application also
     * <b>must</b> include the <code>management</code> fraction in its
     * dependencies.</p>
     *
     * @param path     The keystore path.
     * @param password The keystore password.
     * @param alias    The server certificate alias.
     * @return This fraction.
     */
    public UndertowFraction enableHTTPS(String path, String password, String alias) {
        this.keystorePath = path;
        this.keystorePassword = password;
        this.alias = alias;
        return this;
    }

    /**
     * Enable AJP on this fraction.
     *
     * @return This fraction.
     */
    public UndertowFraction enableAJP() {
        this.enableAJP = true;
        return this;
    }

    /*
    @Override
    public void initialize(Fraction.InitContext initContext) {
        initContext.socketBinding(
                new SocketBinding("http")
                        .port(SwarmProperties.propertyVar(SwarmProperties.HTTP_PORT, "8080")));
        initContext.socketBinding(
                new SocketBinding("https")
                        .port(SwarmProperties.propertyVar(SwarmProperties.HTTPS_PORT, "8443")));
    }
    */

    public String keystorePassword() {
        return this.keystorePassword;
    }

    public String keystorePath() {
        return this.keystorePath;
    }

    public String alias() {
        return this.alias;
    }

    public boolean isEnableAJP() {
        return this.enableAJP;
    }

    /**
     * Path to the keystore.
     */
    private String keystorePath;

    /**
     * Password for the keystore.
     */
    private String keystorePassword;

    /**
     * Server certificate alias.
     */
    private String alias;

    /**
     * Whether or not enabling AJP
     */
    private boolean enableAJP;

    private UndertowFraction removeHttpListenersFromDefaultServer() {
        this.subresources().server("default-server")
                .subresources().httpListeners().clear();
        return this;
    }

}
