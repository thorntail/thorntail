package org.wildfly.swarm.swagger.runtime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContext;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.swagger.SwaggerArchive;
import org.wildfly.swarm.swagger.SwaggerMessages;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 */
@DeploymentScoped
public class SwaggerArchivePreparer implements DeploymentProcessor {

    @Configurable("swarm.deployment.*.swagger.title")
    private String title;

    @Configurable("swarm.deployment.*.swagger.description")
    private String description;

    @Configurable("swarm.deployment.*.swagger.packages")
    private List<String> packages;

    @Configurable("swarm.deployment.*.swagger.tos-url")
    private String tosUrl;

    @Configurable("swarm.deployment.*.swagger.license")
    private String license;

    @Configurable("swarm.deployment.*.swagger.license-url")
    private String licenseUrl;

    @Configurable("swarm.deployment.*.swagger.version")
    private String version;

    @Configurable("swarm.deployment.*.swagger.schemes")
    private List<String> schemes;

    @Configurable("swarm.deployment.*.swagger.host")
    private String host;

    @Configurable("swarm.deployment.*.swagger.root")
    private String root;

    @Configurable("swarm.deployment.*.context.path")
    @Configurable("swarm.context.path")
    Defaultable<String> contextPath = Defaultable.string("/");

    private final Archive archive;

    @Inject
    DeploymentContext deploymentContext;

    @Inject
    public SwaggerArchivePreparer(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() {
        if (this.deploymentContext != null && this.deploymentContext.isImplicit()) {
            return;
        }
        if (archive.getName().endsWith(".war")) {
            // Create a JAX-RS deployment archive
            WARArchive deployment = archive.as(WARArchive.class);
            deployment.addModule("io.swagger");

            // Make the deployment a swagger archive
            SwaggerArchive swaggerArchive = deployment.as(SwaggerArchive.class);

            if (this.title != null) {
                swaggerArchive.setTitle(this.title);
            }

            if (this.description != null) {
                swaggerArchive.setDescription(this.description);
            }

            if (this.packages != null && !this.packages.isEmpty()) {
                swaggerArchive.setResourcePackages(this.packages.toArray(new String[this.packages.size()]));
            }

            if (this.tosUrl != null) {
                swaggerArchive.setTermsOfServiceUrl(this.tosUrl);
            }

            if (this.license != null) {
                swaggerArchive.setLicense(this.license);
            }

            if (this.licenseUrl != null) {
                swaggerArchive.setLicenseUrl(this.licenseUrl);
            }

            if (this.version != null) {
                swaggerArchive.setVersion(this.version);
            }

            if (this.schemes != null && !this.schemes.isEmpty()) {
                swaggerArchive.setSchemes(this.schemes.toArray(new String[this.schemes.size()]));
            }

            if (this.host != null) {
                swaggerArchive.setHost(this.host);
            }

            // If the context root has not been configured
            // get the context root from the deployment and tell swagger about it
            if (this.root != null) {
                swaggerArchive.setContextRoot(this.root);
            } else {
                if (!swaggerArchive.hasContextRoot()) {
                    if (deployment.getContextRoot() != null) {
                        swaggerArchive.setContextRoot(deployment.getContextRoot());
                    } else {
                        swaggerArchive.setContextRoot(contextPath.get());
                    }
                }
            }


            // If the archive has not been configured with packages for swagger to scan
            // try to be smart about it, and find the topmost package that's not in the
            // org.wildfly.swarm package space
            if (!swaggerArchive.hasResourcePackages()) {
                String packageName = null;
                for (Map.Entry<ArchivePath, Node> entry : deployment.getContent().entrySet()) {
                    final ArchivePath key = entry.getKey();
                    if (key.get().endsWith(".class")) {
                        String parentPath = key.getParent().get();
                        parentPath = parentPath.replaceFirst("/", "");

                        String parentPackage = parentPath.replaceFirst(".*/classes/", "");
                        parentPackage = parentPackage.replaceAll("/", ".");

                        if (parentPackage.startsWith("org.wildfly.swarm")) {
                            SwaggerMessages.MESSAGES.ignoringPackage(parentPackage);
                        } else {
                            packageName = parentPackage;
                            break;
                        }
                    }
                }
                if (packageName == null) {
                    SwaggerMessages.MESSAGES.noEligiblePackages(archive.getName());
                } else {
                    SwaggerMessages.MESSAGES.configureSwaggerForPackage(archive.getName(), packageName);
                    swaggerArchive.setResourcePackages(packageName);
                }
            } else {
                SwaggerMessages.MESSAGES.configureSwaggerForSeveralPackages(archive.getName(), Arrays.asList(swaggerArchive.getResourcePackages()));
            }

            // Now add the swagger resources to our deployment
            deployment.addClass(io.swagger.jaxrs.listing.ApiListingResource.class);
            deployment.addClass(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        }
    }
}
