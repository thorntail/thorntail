package org.wildfly.swarm.undertow;

/**
 * @author Bob McWhirter
 */
public interface UndertowProperties {
    int DEFAULT_HTTP_PORT = 8080;
    int DEFAULT_HTTPS_PORT = 8443;
    int DEFAULT_AJP_PORT = 8009;

    String DEFAULT_KEYSTORE_PATH = "application.keystore";
    String DEFAULT_KEYSTORE_PASSWORD = "password";
    String DEFAULT_KEY_PASSWORD = "password";
    String DEFAULT_CERTIFICATE_ALIAS = "server";

    String DEFAULT_SERVER = "default-server";
    String DEFAULT_HTTP_LISTENER = "default";
    String DEFAULT_HTTPS_LISTENER = "default-https";
    String DEFAULT_HOST = "default-host";
}
