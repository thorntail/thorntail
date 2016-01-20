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

        String[] lines = written.split("(\\r)?(\\n)");

        assertThat( lines ).hasSize(3);
        assertThat( lines ).contains( "gav:org.wildfly.swarm:fish:1.0" );
        assertThat( lines ).contains( "module:com.mymodule:main");
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
