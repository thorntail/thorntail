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
package org.wildfly.swarm.swagger;

import org.jboss.shrinkwrap.api.Assignable;

/**
 * @author Lance Ball
 */
public interface SwaggerArchive extends Assignable {
    String SWAGGER_CONFIGURATION_PATH = "META-INF/swarm.swagger.conf";

    public SwaggerArchive setResourcePackages(String... packages);

    /**
     * Sets the title of the application being exposed by swagger.json
     *
     * @param title the application's title
     * @return this
     */
    public SwaggerArchive setTitle(String title);

    /**
     * Sets the description for the application being exposed by swagger.json
     *
     * @param description the description
     * @return this
     */
    public SwaggerArchive setDescription(String description);

    /**
     * Sets the url to be displayed for the application's term's of service
     *
     * @param url the URL string
     * @return this
     */
    public SwaggerArchive setTermsOfServiceUrl(String url);

    /**
     * Sets the contact information for the application
     *
     * @param info The contact information string
     * @return this
     */
    public SwaggerArchive setContact(String info);

    /**
     * Sets the license of the application.
     *
     * @param license the license text
     * @return this
     */
    public SwaggerArchive setLicense(String license);

    /**
     * Sets the license URL for this application
     *
     * @param licenseUrl the license URL as a string
     * @return this
     */
    public SwaggerArchive setLicenseUrl(String licenseUrl);

    /**
     * Sets the version of the API being exposed for this application
     *
     * @param version the version string
     * @return this
     */
    public SwaggerArchive setVersion(String version);

    /**
     * Sets the schemes for the for the API URLs (http, https)
     *
     * @param schemes One or more strings. Valid values are 'http' and 'https'
     * @return this
     */
    public SwaggerArchive setSchemes(String... schemes);

    /**
     * Sets the addressable host name and port for the API URLs.
     * Does not include the schemes nor context root.
     *
     * @param host the host name
     * @return this
     */
    public SwaggerArchive setHost(String host);

    /**
     * Sets the context root, or base path for the API calls.
     *
     * @param root the context root
     * @return this
     */
    public SwaggerArchive setContextRoot(String root);

    /**
     * Sets whether the swagger.json will be pretty printed.
     *
     * @param prettyPrint if true swagger.json will be pretty printed
     * @return this
     */
    public SwaggerArchive setPrettyPrint(boolean prettyPrint);

    /**
     * Determine if the archive has been configured for scanning
     *
     * @return true if the packages to be scanned have been configured
     */
    public boolean hasResourcePackages();

    public String[] getResourcePackages();
}
