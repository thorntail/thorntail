package org.wildfly.swarm.container;

import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Bob McWhirter
 */
public class DefaultDeployment implements Deployment {

    public String getName() {
        return System.getProperty("wildfly.swarm.app.name");
    }

    @Override
    public VirtualFile getContent() throws IOException {

        String path = System.getProperty("wildfly.swarm.app.path");
        File file = new File(path);

        VirtualFile mountPoint = VFS.getRootVirtualFile().getChild(file.getName());

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        TempFileProvider tempProvider = TempFileProvider.create("wildfly-swarm", executor);
        VFS.mountZip(file, mountPoint, tempProvider);

        ensureJBossWebXml(mountPoint);

        return mountPoint;
    }

    public static void ensureJBossWebXml(VirtualFile archiveRoot) throws IOException {
        if (!archiveRoot.getName().endsWith(".war")) {
            return;
        }
        VirtualFile jbossWeb = archiveRoot.getChild("WEB-INF/jboss-web.xml");
        if (!jbossWeb.exists()) {
            File jbossWebTmp = File.createTempFile("jboss-web", "xml");
            FileWriter out = new FileWriter(jbossWebTmp);
            try {
                out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<jboss-web>\n" +
                        "    <context-root>/</context-root>\n" +
                        "</jboss-web>");
            } finally {
                out.close();
            }

            VFS.mountReal(jbossWebTmp, jbossWeb);
        }
    }
}
