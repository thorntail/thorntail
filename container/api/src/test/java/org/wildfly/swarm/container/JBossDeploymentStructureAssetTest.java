package org.wildfly.swarm.container;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.wildfly.swarm.container.InputStreamHelper.*;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class JBossDeploymentStructureAssetTest {


    @Test
    public void testEmpty() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();
        asset.addModule("com.mycorp", "main");

        List<String> lines = read(asset.openStream());

        assertThat( lines ).contains( "<module name=\"com.mycorp\" slot=\"main\"/>");
    }

    @Test
    public void testAdditive() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();
        asset.addModule("com.mycorp", "main");

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset( asset.openStream() );
        asset2.addModule( "com.mycorp.another", "api" );

        List<String> lines = read(asset2.openStream());

        assertThat( lines ).contains( "<module name=\"com.mycorp\" slot=\"main\"/>");
        assertThat( lines ).contains( "<module name=\"com.mycorp.another\" slot=\"api\"/>");

    }


}
