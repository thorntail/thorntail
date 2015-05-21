package org.wildfly.swarm.container;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Bob McWhirter
 */
public class WarDeployment implements Deployment {

    private final static String JBOSS_WEB_CONTENTS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<jboss-web>\n" +
                    "    <context-root>/</context-root>\n" +
                    "</jboss-web>";

    protected final WebArchive archive;

    public WarDeployment(WebArchive archive) throws IOException, ModuleLoadException {
        this.archive = archive;
    }

    protected void ensureJBossWebXml() {
        if (this.archive.contains("WEB-INF/jboss-web.xml")) {
            return;
        }

        this.archive.add(new StringAsset(JBOSS_WEB_CONTENTS), "WEB-INF/jboss-web.xml");
    }

    public WebArchive getArchive() {
        ensureJBossWebXml();
        return this.archive;
    }
}
