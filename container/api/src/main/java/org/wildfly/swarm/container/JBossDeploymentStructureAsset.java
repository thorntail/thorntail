package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class JBossDeploymentStructureAsset implements Asset{


    private final static String JBOSS_DEPLOYMENT_STRUCTURE_CONTENTS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>  \n" +
                    "<jboss-deployment-structure>  \n" +
                    "    <deployment>  \n" +
                    "         <dependencies>  \n" +
                    "              ${MODULES}\n" +
                    "        </dependencies>  \n" +
                    "    </deployment>  \n" +
                    "</jboss-deployment-structure>\n";

    private Set<String> modules = new HashSet<>();

    public JBossDeploymentStructureAsset() {

    }

    public void addModule(String name, String slot) {
        this.modules.add( name + ":" + slot );
    }

    @Override
    public InputStream openStream() {
        StringBuilder modules = new StringBuilder();
        for ( String each : this.modules ) {
            String[] parts = each.split(":");
            modules.append( "              <module name=\"" + parts[0] + "\" slot=\"" + parts[1] + "\"/>\n");
        }
        String structureContents = JBOSS_DEPLOYMENT_STRUCTURE_CONTENTS.replace( "${MODULES}", modules.toString().trim() );

        /*
        System.err.println( "----" );
        System.err.println( structureContents );
        System.err.println( "----" );
        */
        return new ByteArrayInputStream( structureContents.getBytes() );
    }
}
