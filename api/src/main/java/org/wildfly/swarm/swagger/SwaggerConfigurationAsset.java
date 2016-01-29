package org.wildfly.swarm.swagger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author Lance Ball
 */
public class SwaggerConfigurationAsset implements Asset {

    private final SwaggerConfig configuration;

    public SwaggerConfigurationAsset() {
        configuration = new SwaggerConfig();
    }

    public SwaggerConfigurationAsset(InputStream is) {
        configuration = new SwaggerConfig(is);
    }

    @Override
    public InputStream openStream() {
        StringBuilder builder = new StringBuilder();
        Set<Map.Entry<SwaggerConfig.Key, Object>> entries = configuration.entrySet();

        for (Map.Entry entry : entries) {
            Object value = entry.getValue();
            if (value != null) {
                builder.append(entry.getKey() + ":" + valueFor(value) + "\n");
            }
        }
        return new ByteArrayInputStream(builder.toString().getBytes());
    }

    public SwaggerConfigurationAsset register(String[] packageNames) {
        configuration.put(SwaggerConfig.Key.PACKAGES, packageNames);
        return this;
    }

    public SwaggerConfigurationAsset setTitle(String title) {
        configuration.put(SwaggerConfig.Key.TITLE, title);
        return this;
    }

    public SwaggerConfigurationAsset setDescription(String description) {
        configuration.put(SwaggerConfig.Key.DESCRIPTION, description);
        return this;
    }

    public SwaggerConfigurationAsset setTermsOfServiceUrl(String termsOfServiceUrl) {
        configuration.put(SwaggerConfig.Key.TERMS_OF_SERVICE_URL, termsOfServiceUrl);
        return this;
    }


    public SwaggerConfigurationAsset setContact(String contact) {
        configuration.put(SwaggerConfig.Key.CONTACT, contact);
        return this;
    }

    public SwaggerConfigurationAsset setLicense(String license) {
        configuration.put(SwaggerConfig.Key.LICENSE, license);
        return this;
    }

    public SwaggerConfigurationAsset setLicenseUrl(String licenseUrl) {
        configuration.put(SwaggerConfig.Key.LICENSE_URL, licenseUrl);
        return this;
    }

    public SwaggerConfigurationAsset setVersion(String version) {
        configuration.put(SwaggerConfig.Key.VERSION, version);
        return this;
    }

    public SwaggerConfigurationAsset setSchemes(String[] schemes) {
        configuration.put(SwaggerConfig.Key.SCHEMES, schemes);
        return this;
    }

    public SwaggerConfigurationAsset setHost(String host) {
        configuration.put(SwaggerConfig.Key.HOST, host);
        return this;
    }

    public SwaggerConfigurationAsset setContextRoot(String root) {
        configuration.put(SwaggerConfig.Key.ROOT, root);
        return this;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        configuration.put(SwaggerConfig.Key.PRETTY_PRINT, prettyPrint);
    }

    private String valueFor(Object value) {
        if (value instanceof String[]) {
            StringBuilder buf = new StringBuilder();
            for (String name : (String[]) value) {
                buf.append(name).append(",");
            }
            // remove last comma
            buf.setLength(Math.max(buf.length() - 1, 0));
            return buf.toString();
        }
        return value.toString();
    }


    public String[] getResourcePackages() {
        return (String[]) configuration.get(SwaggerConfig.Key.PACKAGES);
    }
}
