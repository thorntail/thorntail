package org.wildfly.swarm.container;

import org.jboss.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public interface Deployment {
    String getName();
    VirtualFile getContent() throws IOException;
}
