package org.wildfly.swarm.microprofile.jwtauth.deployment.principal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.jboss.logging.Logger;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.UnresolvableKeyException;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.KeyUtils;

/**
 * This implements the MP-JWT 1.1 mp.jwt.verify.publickey.location config property resolution logic
 */
public class KeyLocationResolver implements VerificationKeyResolver {
    private static final Logger log = Logger.getLogger(KeyLocationResolver.class);
    private String location;
    private StringWriter contents;
    private byte[] base64Decode;

    public KeyLocationResolver(String location) {
        this.location = location;
    }
    @Override
    public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
        PublicKey key = null;
        String kid = jws.getHeaders().getStringHeaderValue("kid");
        try {
            loadContents();
        } catch (IOException e) {
            throw new UnresolvableKeyException("Failed to load key from: " + location, e);
        }
        // Determine what the contents are...
        key = tryAsJWKx(kid);
        if (key == null) {
            key = tryAsPEM();
        }
        if (key == null) {
            throw new UnresolvableKeyException("Failed to read location as any of JWK, JWKS, PEM; " + location);
        }
        return key;
    }

    private PublicKey tryAsPEM() {
        PublicKey publicKey = null;
        try {
            publicKey = KeyUtils.decodePublicKey(contents.toString());
        } catch (Exception e) {
            log.debug("Failed to read location as PEM", e);
        }
        return publicKey;
    }

    private PublicKey tryAsJWKx(String kid) {
        PublicKey publicKey = null;
        try {
            log.debugf("Trying location as JWK(S)...");
            String json;
            if (base64Decode != null) {
                json = new String(base64Decode);
            } else {
                json = contents.toString();
            }
            JsonObject jwks = Json.createReader(new StringReader(json)).readObject();
            JsonArray keys = jwks.getJsonArray("keys");
            JsonObject jwk;
            if (keys != null) {
                jwk = keys.getJsonObject(0);
            } else {
                jwk = jwks;
            }
            String e = jwk.getString("e");
            String n = jwk.getString("n");

            byte[] ebytes = Base64.getUrlDecoder().decode(e);
            BigInteger publicExponent = new BigInteger(1, ebytes);
            byte[] nbytes = Base64.getUrlDecoder().decode(n);
            BigInteger modulus = new BigInteger(1, nbytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
            publicKey = kf.generatePublic(rsaPublicKeySpec);
        } catch (Exception e) {
            log.debug("Failed to read location as JWK(S)", e);
        }

        return publicKey;
    }

    private void loadContents() throws IOException {
        contents = new StringWriter();
        InputStream is;
        if (location.startsWith("classpath:") || location.indexOf(':') < 0) {
            String path;
            if (location.startsWith("classpath:")) {
                path = location.substring(10);
            } else {
                path = location;
            }
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            is = loader.getResourceAsStream(path);
        } else {
            URL locationURL = new URL(location);
            is = locationURL.openStream();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("-----BEGIN") && !line.startsWith("-----END")) {
                    // Skip any pem file header/footer
                    contents.write(line);
                }
                line = reader.readLine();
            }
        }
        try {
            // Determine if this is base64
            base64Decode = Base64.getDecoder().decode(contents.toString());
        } catch (Exception e) {
            base64Decode = null;
            log.debug("contents does not appear to be base64 encoded");
        }
    }
}
