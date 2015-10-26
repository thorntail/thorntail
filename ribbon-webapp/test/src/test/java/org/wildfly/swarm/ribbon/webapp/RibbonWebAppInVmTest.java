package org.wildfly.swarm.ribbon.webapp;

import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Lance Ball
 */
public class RibbonWebAppInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new RibbonWebAppFraction() );
        container.start().stop();
    }
}
