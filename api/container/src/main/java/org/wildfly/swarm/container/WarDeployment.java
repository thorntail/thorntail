package org.wildfly.swarm.container;

import java.io.File;
import java.io.IOException;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author Bob McWhirter
 */
public class WarDeployment implements Deployment {

    private final static String JBOSS_WEB_CONTENTS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<jboss-web>\n" +
                    "    <context-root>/</context-root>\n" +
                    "</jboss-web>";

    private final static String JBOSS_DEPLOYMENT_STRUCTURE_CONTENTS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>  \n" +
                    "<jboss-deployment-structure>  \n" +
                    "    <deployment>  \n" +
                    "         <dependencies>  \n" +
                    "              <module name=\"APP\" slot=\"dependencies\" />  \n" +
                    "        </dependencies>  \n" +
                    "    </deployment>  \n" +
                    "</jboss-deployment-structure>\n";

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

    protected void ensureJBossDeploymentStructureXml() {
        if (this.archive.contains("WEB-INF/jboss-deployment-structure.xml")) {
            return;
        }

        this.archive.add(new StringAsset(JBOSS_DEPLOYMENT_STRUCTURE_CONTENTS), "WEB-INF/jboss-deployment-structure.xml");
    }

    protected void addJavaClassPathToWebInfLib() {
        String classpath = System.getProperty("java.class.path");
        String javaHome = System.getProperty("java.home");
        if (classpath != null) {
            String[] elements = classpath.split(File.pathSeparator);

            for (int i = 0; i < elements.length; ++i) {
                if (!elements[i].startsWith(javaHome)) {
                    File file = new File(elements[i]);
                    if (file.isFile()) {
                        this.archive.add(new FileAsset(file), "WEB-INF/lib/" + file.getName());
                    }
                }
            }
        }
    }

    public WebArchive getArchive() {
        ensureJBossWebXml();
        ensureJBossDeploymentStructureXml();
        return this.archive;
    }
}
