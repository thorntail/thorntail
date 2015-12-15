package org.wildfly.swarm.swagger;

import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.swagger.SwaggerFraction;

/**
 * @author Lance Ball
 */
public class SwaggerInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new SwaggerFraction() );
        container.start().stop();
    }
}
