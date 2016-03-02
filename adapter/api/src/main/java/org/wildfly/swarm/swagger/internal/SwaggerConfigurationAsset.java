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
package org.wildfly.swarm.swagger.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.wildfly.swarm.swagger.SwaggerConfig;

/**
 * @author Lance Ball
 */
public class SwaggerConfigurationAsset implements Asset {

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

    public String[] getResourcePackages() {
        return (String[]) configuration.get(SwaggerConfig.Key.PACKAGES);
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

    private final SwaggerConfig configuration;
}
