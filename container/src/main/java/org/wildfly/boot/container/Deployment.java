package org.wildfly.boot.container;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public interface Deployment {

    File getContent() throws IOException;
}
