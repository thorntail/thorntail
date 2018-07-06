package io.thorntail.jwt.auth.impl.undertow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import io.thorntail.jwt.auth.impl.KeyUtils;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanismFactory;
import io.undertow.server.handlers.form.FormParserFactory;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.jboss.logging.Logger;
import io.thorntail.jwt.auth.impl.JWTAuthContextInfo;

/**
 * An AuthenticationMechanismFactory for the MicroProfile JWT RBAC
 */
@ApplicationScoped
public class JWTAuthMechanismFactory implements AuthenticationMechanismFactory {
    private static Logger log = Logger.getLogger(JWTAuthMechanismFactory.class);


    @PostConstruct
    public void init() {
        log.debugf("init");
    }

    /**
     * This builds the JWTAuthMechanism with a JWTAuthContextInfo containing the issuer and signer public key needed
     * to validate the token. This information is currently taken from the query parameters passed in via the
     * web.xml/login-config/auth-method value, or via CDI injection.
     *
     * @param mechanismName     - the login-config/auth-method, which will be MP-JWT for JWTAuthMechanism
     * @param formParserFactory - unused form type of authentication factory
     * @param properties        - the query parameters from the web.xml/login-config/auth-method value. We look for an issuedBy
     *                          and signerPubKey property to use for token validation.
     * @return the JWTAuthMechanism
     * @see JWTAuthContextInfo
     */
    @Override
    public AuthenticationMechanism create(String mechanismName, FormParserFactory formParserFactory, Map<String, String> properties) {
        System.err.println("***** create: " + mechanismName);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        JWTAuthContextInfo contextInfo;
        Optional<JWTAuthContextInfo> optContextInfo = Optional.empty();
        try {
            Instance<JWTAuthContextInfo> contextInfoInstance = CDI.current().select(JWTAuthContextInfo.class);
            contextInfo = contextInfoInstance.get();
            optContextInfo = Optional.of(contextInfo);
        } catch (Exception e) {
            log.debugf(e, "Unable to select JWTAuthContextInfo provider");
        }

        if (!optContextInfo.isPresent()) {
            // Try building the JWTAuthContextInfo from the properties and/or the deployment resources
            contextInfo = new JWTAuthContextInfo();
            String issuedBy = properties.get("issuedBy");
            if (issuedBy == null) {
                Config config = ConfigProviderResolver.instance().getConfig();
                Optional<String> c = config.getOptionalValue("jwt.issuedBy", String.class);
                if (c.isPresent()) {
                    issuedBy = c.get();
                }
            }
            if (issuedBy == null) {
                // Try the /META-INF/MP-JWT-ISSUER content
                URL issURL = loader.getResource("/META-INF/MP-JWT-ISSUER");
                if (issURL == null) {
                    throw new IllegalStateException("No issuedBy parameter was found");
                }
                issuedBy = readURLContent(issURL);
                if (issuedBy == null) {
                    throw new IllegalStateException("No issuedBy parameter was found");
                }
                issuedBy = issuedBy.trim();
            }
            String publicKeyPemEnc = properties.get("signerPubKey");
            if (publicKeyPemEnc == null) {
                // Try the /META-INF/MP-JWT-SIGNER content
                System.err.println("LOADER: " + loader);
                URL pkURL = loader.getResource("META-INF/MP-JWT-SIGNER");
                System.err.println( "--> " + pkURL);
                if (pkURL == null) {
                    throw new IllegalStateException("No signerPubKey parameter was found");
                }
                publicKeyPemEnc = readURLContent(pkURL);
            }

            // Workaround the double decode issue; https://issues.jboss.org/browse/WFLY-9135
            String publicKeyPem = publicKeyPemEnc.replace(' ', '+');
            contextInfo.setIssuedBy(issuedBy);
            try {
                RSAPublicKey pk = (RSAPublicKey) KeyUtils.decodePublicKey(publicKeyPem);
                contextInfo.setSignerKey(pk);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            contextInfo = optContextInfo.get();
        }

        return new JWTAuthMechanism(contextInfo);
    }

    private String readURLContent(URL url) {
        StringBuilder content = new StringBuilder();
        try {
            InputStream is = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                content.append(line);
                content.append('\n');
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            log.warnf("Failed to read content from: %s, error=%s", url, e.getMessage());
        }
        return content.toString();
    }

}
