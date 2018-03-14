package org.wildfly.swarm.swagger.webapp.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.swagger.webapp.SwaggerWebAppFraction;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class SwaggerWebAppDeploymentProducer {

    private static final Pattern PATTERN = Pattern.compile("/META-INF/resources/webjars/swagger-ui/([^\\/]+/)(.*)");

    @Inject
    @Any
    private SwaggerWebAppFraction fraction;

    @Produces
    public Archive swaggerWebApp() throws ModuleLoadException, IOException {

        // Load the swagger-ui webjars.
        Module module = Module.getBootModuleLoader().loadModule("org.webjars.swagger-ui");
        URL resource = module.getExportedResource("swagger-ui.jar");
        JARArchive webJar = ShrinkWrap.create(JARArchive.class);
        webJar.as(ZipImporter.class).importFrom(resource.openStream());


        JARArchive relocatedJar = ShrinkWrap.create(JARArchive.class);

        Map<ArchivePath, Node> content = webJar.getContent();

        // relocate out the webjars/swagger-ui/${VERISON}

        for (ArchivePath path : content.keySet()) {
            Node node = content.get(path);
            Asset asset = node.getAsset();
            if (asset != null) {
                Matcher matcher = PATTERN.matcher(path.get());
                if (matcher.matches()) {
                    MatchResult result = matcher.toMatchResult();
                    String newPath = "/META-INF/resources/" + result.group(2);
                    relocatedJar.add(asset, newPath);
                }
            }
        }

        WARArchive war = ShrinkWrap.create(WARArchive.class, "swagger-ui.war")
                .addAsLibrary(relocatedJar)
                .setContextRoot(this.fraction.getContext());
        war.addClass(SwaggerDefaultUrlChangerServlet.class);

        // If any user content has been provided, merge that with the swagger-ui bits
        Archive<?> userContent = this.fraction.getWebContent();
        if (userContent != null) {
            war.merge(userContent);
        }

        return war;
    }
}
