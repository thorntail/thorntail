package org.wildfly.swarm.javafx;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Ken Finnigan
 */
public class JavaFXInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction(new JavaFXFraction());
        container.start().stop();
    }
}
