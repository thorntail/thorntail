package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ModuleXmlParserBridge;
import org.jboss.modules.ResourceLoaderSpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Bob McWhirter
 */
public class AppDependenciesModuleFinder implements ModuleFinder {
    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        if (!(identifier.getName().equals("APP") && identifier.getSlot().equals("dependencies"))) {
            return null;
        }

        ModuleSpec.Builder builder = ModuleSpec.build(identifier);

        InputStream depsTxt = ClassLoader.getSystemClassLoader().getResourceAsStream("META-INF/wildfly-swarm-dependencies.txt");

        if (depsTxt != null) {
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(depsTxt)) ) {

                String line = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(ModuleXmlParserBridge.createMavenArtifactLoader(line)));

                        String[] parts = line.split(":");
                        String groupId = parts[0];
                        String artifactId = parts[1];
                        String version = parts[2];
                        String classifier = "";
                        if ( parts.length > 3 ) {
                            classifier = parts[3];
                        }
                        builder.addProperty( "version." + groupId + ":" + artifactId + "::" + classifier, version );
                    }
                }
                depsTxt.close();
            } catch (IOException e) {
                throw new ModuleLoadException("Error loading wildfly-swarm-dependencies.txt", e);
            }
            builder.addDependency(DependencySpec.createLocalDependencySpec());
        }

        return builder.create();
    }
}
