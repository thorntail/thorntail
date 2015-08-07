package org.wildfly.swarm.jaxrs;

import java.io.IOException;
import java.security.SecureClassLoader;

import org.junit.Test;

/**
 * @author Bob McWhirter
 */
public class FaviconExceptionMapperFactoryTest {

    @Test
    public void testCreate() throws IOException {
        byte[] bytes = FaviconExceptionMapperFactory.create();
    }
}
