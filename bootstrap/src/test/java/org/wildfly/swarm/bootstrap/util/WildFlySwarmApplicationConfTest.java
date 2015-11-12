package org.wildfly.swarm.bootstrap.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmApplicationConfTest {

    @Test
    public void testRoundTripWriteRead() throws Exception {
        WildFlySwarmApplicationConf appConf = new WildFlySwarmApplicationConf();

        appConf.addEntry( new WildFlySwarmApplicationConf.GAVEntry( MavenArtifactDescriptor.fromMscGav("org.wildfly.swarm:fish:1.0")) );
        appConf.addEntry( new WildFlySwarmApplicationConf.ModuleEntry( "com.mymodule" ) );
        appConf.addEntry( new WildFlySwarmApplicationConf.PathEntry( "_bootstrap/myapp.war" ) );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        appConf.write(out);
        out.close();

        String written = new String( out.toByteArray() );

        String[] lines = written.split("\n");

        assertThat( lines ).hasSize(3);
        assertThat( lines ).contains( "gav:org.wildfly.swarm:fish:1.0" );
        assertThat( lines ).contains( "module:com.mymodule");
        assertThat( lines ).contains( "path:_bootstrap/myapp.war" );

        ByteArrayInputStream in = new ByteArrayInputStream( written.getBytes() );

        appConf = new WildFlySwarmApplicationConf( in );

        assertThat( appConf.getEntries() ).hasSize(3);

        WildFlySwarmApplicationConf.GAVEntry gavEntry = (WildFlySwarmApplicationConf.GAVEntry) appConf.getEntries().stream().filter(e->e instanceof WildFlySwarmApplicationConf.GAVEntry ).findFirst().get();
        assertThat( gavEntry.getDescriptor().mscGav() ).isEqualTo( "org.wildfly.swarm:fish:1.0" );

        WildFlySwarmApplicationConf.ModuleEntry moduleEntry = (WildFlySwarmApplicationConf.ModuleEntry) appConf.getEntries().stream().filter(e->e instanceof WildFlySwarmApplicationConf.ModuleEntry ).findFirst().get();
        assertThat( moduleEntry.getName() ).isEqualTo( "com.mymodule");

        WildFlySwarmApplicationConf.PathEntry pathEntry = (WildFlySwarmApplicationConf.PathEntry) appConf.getEntries().stream().filter(e->e instanceof WildFlySwarmApplicationConf.PathEntry ).findFirst().get();
        assertThat( pathEntry.getPath() ).isEqualTo( "_bootstrap/myapp.war" );

    }
}
