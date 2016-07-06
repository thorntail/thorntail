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
package org.wildfly.swarm.swagger.webapp.runtime;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.Resource;
import org.jboss.modules.filter.PathFilters;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.swagger.webapp.SwaggerWebAppFraction;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Lance Ball
 */
public class SwaggerWebAppConfiguration extends AbstractServerConfiguration<SwaggerWebAppFraction> {
    private static final Pattern PATTERN = Pattern.compile( "/META-INF/resources/webjars/swagger-ui/([^\\/]+/)(.*)" );

    public SwaggerWebAppConfiguration() {
        super(SwaggerWebAppFraction.class);
    }

    @Override
    public List<Archive> getImplicitDeployments(SwaggerWebAppFraction fraction) throws Exception {
        List<Archive> list = new ArrayList<>();
        try {
            // Load the swagger-ui webjars.
            Module module = Module.getBootModuleLoader().loadModule( ModuleIdentifier.create( "org.webjars.swagger-ui" ) );
            URL resource = module.getExportedResource("swagger-ui.jar");
            JARArchive webJar = ShrinkWrap.create(JARArchive.class);
            webJar.as(ZipImporter.class).importFrom( resource.openStream() );


            JARArchive relocatedJar = ShrinkWrap.create( JARArchive.class );

            Map<ArchivePath, Node> content = webJar.getContent();

            // relocate out the webjars/swagger-ui/${VERISON}

            for (ArchivePath path : content.keySet()) {
                Node node = content.get(path);
                Asset asset = node.getAsset();
                if ( asset != null ) {
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
                    .setContextRoot(fraction.getContext());

            // If any user content has been provided, merge that with the swagger-ui bits
            Archive<?> userContent = fraction.getWebContent();
            if (userContent != null) {
                war.merge(userContent);
            }
            //war.as(ZipExporter.class).exportTo(new File("swagger-webapp-ui.war"), true);
            list.add(war);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public SwaggerWebAppFraction defaultFraction() {
        return new SwaggerWebAppFraction();
    }
}
