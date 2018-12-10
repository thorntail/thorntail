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
package org.wildfly.swarm.undertow;

import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.config.undertow.BufferCache;
import org.wildfly.swarm.config.undertow.HandlerConfiguration;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.ServletContainer;
import org.wildfly.swarm.config.undertow.server.Host;
import org.wildfly.swarm.config.undertow.servlet_container.JSPSetting;
import org.wildfly.swarm.config.undertow.servlet_container.WebsocketsSetting;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.ifAnyExplicitlySet;
import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.undertow.UndertowProperties.DEFAULT_AJP_PORT;
import static org.wildfly.swarm.undertow.UndertowProperties.DEFAULT_BUFFER_CACHE;
import static org.wildfly.swarm.undertow.UndertowProperties.DEFAULT_HOST;
import static org.wildfly.swarm.undertow.UndertowProperties.DEFAULT_HTTPS_PORT;
import static org.wildfly.swarm.undertow.UndertowProperties.DEFAULT_HTTP_LISTENER;
import static org.wildfly.swarm.undertow.UndertowProperties.DEFAULT_HTTP_PORT;
import static org.wildfly.swarm.undertow.UndertowProperties.DEFAULT_SERVER;
import static org.wildfly.swarm.undertow.UndertowProperties.DEFAULT_SERVLET_CONTAINER;

/**
 * @author Bob McWhirter
 */
@MarshalDMR
@WildFlyExtension(module = "org.wildfly.extension.undertow")
@DeploymentModule(name = "org.jboss.modules")
public class UndertowFraction extends Undertow<UndertowFraction> implements Fraction {

    /**
     * Create the default, HTTP-only fraction.
     *
     * @return The configured fraction.
     */
    public static UndertowFraction createDefaultFraction() {
        UndertowFraction fraction = new UndertowFraction();
        return fraction.applyDefaults();
    }

    public UndertowFraction applyDefaults() {
        defaultServer(DEFAULT_SERVER)
                .defaultVirtualHost(DEFAULT_HOST)
                .defaultServletContainer(DEFAULT_SERVLET_CONTAINER)
                .defaultSecurityDomain("other")
                .server(new Server(DEFAULT_SERVER)
                        .httpListener(DEFAULT_HTTP_LISTENER, (listener) -> {
                            listener.socketBinding("http");
                        })
                        .host(new Host(DEFAULT_HOST)))
                .bufferCache(new BufferCache(DEFAULT_BUFFER_CACHE))
                .servletContainer(new ServletContainer(DEFAULT_SERVLET_CONTAINER)
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
        return enableHTTPS(path, password, password, alias);
    }

    /**
     * Enable HTTPS on this fraction.
     *
     * <p>This will enable HTTPS of the fraction. The application also
     * <b>must</b> include the <code>management</code> fraction in its
     * dependencies.</p>
     *
     * @param path             The keystore path.
     * @param keystorePassword The keystore password.
     * @param keyPassword      The key password inside the keystore.
     * @param alias            The server certificate alias.
     * @return This fraction.
     */
    public UndertowFraction enableHTTPS(String path, String keystorePassword, String keyPassword, String alias) {
        this.keystorePath = path;
        this.keystorePassword = keystorePassword;
        this.keyPassword = keyPassword;
        this.alias = alias;
        return this;
    }

    /**
     * Enable AJP on this fraction.
     *
     * @return This fraction.
     */
    public UndertowFraction enableAJP() {
        this.enableAJP.set(true);
        return this;
    }

    public UndertowFraction onlyHTTPS() {
        this.onlyHTTPS.set(true);
        return this;
    }

    public String keystorePassword() {
        return this.keystorePassword;
    }

    public String keyPassword() {
        return this.keyPassword;
    }

    public String keystorePath() {
        return this.keystorePath;
    }

    public String alias() {
        return this.alias;
    }

    public boolean isOnlyHTTPS() {
        return this.onlyHTTPS.get();
    }

    public boolean isEnableAJP() {
        return this.enableAJP.get();
    }

    public UndertowFraction removeHttpListenersFromDefaultServer() {
        this.subresources().server("default-server")
                .subresources().httpListeners().clear();
        return this;
    }

    public UndertowFraction httpPort(int httpPort) {
        this.httpPort.set(httpPort);
        return this;
    }

    public int httpPort() {
        return this.httpPort.get();
    }

    public UndertowFraction httpsPort(int httpsPort) {
        this.httpsPort.set(httpsPort);
        return this;
    }

    public int httpsPort() {
        return this.httpsPort.get();
    }

    public UndertowFraction ajpPort(int ajpPort) {
        this.ajpPort.set(ajpPort);
        return this;
    }

    public int ajpPort() {
        return this.ajpPort.get();
    }

    @Configurable("thorntail.http.port")
    @AttributeDocumentation("Set the port for the default HTTP listener")
    private Defaultable<Integer> httpPort = integer(DEFAULT_HTTP_PORT);

    @Configurable("thorntail.https.port")
    @AttributeDocumentation("Set the port for the default HTTPS listener")
    private Defaultable<Integer> httpsPort = integer(DEFAULT_HTTPS_PORT);

    @Configurable("thorntail.ajp.port")
    @AttributeDocumentation("Set the port for the default AJP listener")
    private Defaultable<Integer> ajpPort = integer(DEFAULT_AJP_PORT);

    /**
     * Path to the keystore.
     */
    @Configurable("thorntail.https.keystore.path")
    @Configurable("thorntail.http.keystore.path")
    @AttributeDocumentation("Path to the server keystore")
    private String keystorePath;

    /**
     * Password for the keystore.
     */
    @Configurable("thorntail.https.keystore.password")
    @Configurable("thorntail.http.keystore.password")
    @AttributeDocumentation("Password to the server keystore")
    private String keystorePassword;

    /**
     * Password for the key.
     */
    @Configurable("thorntail.https.key.password")
    @Configurable("thorntail.http.key.password")
    @AttributeDocumentation("Password to the server certificate")
    private String keyPassword;

    /**
     * Alias of Server certificate key entry in the keystore.
     */
    @Configurable("thorntail.https.key.alias")
    @Configurable("thorntail.http.certificate.alias")
    @AttributeDocumentation("Alias to the server certificate key entry in the keystore")
    private String alias;

    /**
     * Whether or not disable HTTP interface
     */
    @Configurable("thorntail.https.only")
    @AttributeDocumentation("Only enable the HTTPS  Listener")
    private Defaultable<Boolean> onlyHTTPS = bool(false);

    /**
     * Whether or not enabling AJP
     */
    @Configurable("thorntail.ajp.enable")
    @AttributeDocumentation("Determine if AJP should be enabled")
    private Defaultable<Boolean> enableAJP = ifAnyExplicitlySet(this.ajpPort);

}
