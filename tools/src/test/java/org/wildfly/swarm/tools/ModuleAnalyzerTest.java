package org.wildfly.swarm.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ModuleAnalyzerTest {

    @Test
    public void testAnalysis() throws Exception {
        InputStream moduleXml = getClass().getClassLoader().getResourceAsStream( "module.xml" );
        ModuleAnalyzer analyzer = new ModuleAnalyzer( moduleXml );
        assertThat( analyzer.getDependencies() ).hasSize(2);
        List<String> gavs = analyzer.getDependencies().stream().map(e -> e.mscGav()).collect(Collectors.toList());
        assertThat( gavs ).contains( "org.wildfly:wildfly-webservices-server-integration:10.0.0.CR4" );
        assertThat( gavs ).contains( "org.jboss.ws.cxf:jbossws-cxf-resources:5.1.0.Final:wildfly1000" );

        assertThat( analyzer.getDependencies().stream().allMatch( e->e.shouldGather ));
    }

    @Test
    public void testAvoidAliases() throws IOException {
        InputStream moduleXml = getClass().getClassLoader().getResourceAsStream( "alias-module.xml" );
        ModuleAnalyzer analyzer = new ModuleAnalyzer( moduleXml );
        assertThat( analyzer.getDependencies() ).hasSize(0);
    }
}
