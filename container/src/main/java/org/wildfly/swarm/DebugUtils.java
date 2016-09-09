package org.wildfly.swarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.JBossDeploymentStructureAsset;
import org.wildfly.swarm.spi.api.JBossDeploymentStructureContainer;

/**
 * @author Bob McWhirter
 */
public class DebugUtils {

    public static void dumpJBossDeploymentStructure(Archive archive) {
        System.err.println( "--- start jboss-deployment-structure.xml" );
        JBossDeploymentStructureAsset asset = archive.as(JARArchive.class).getDescriptorAsset();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(asset.openStream()))) {
            reader.lines().forEach(line -> System.err.println(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println( "--- end jboss-deployment-structure.xml" );
    }
}
