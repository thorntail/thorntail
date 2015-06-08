package org.wildfly.swarm.container;

import org.jboss.modules.MavenArtifactUtil;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/** A WAR-centric deployment.
 *
 * <p>The deployment handles some common activities if not provided by the
 * application:</p>
 *
 * <ul>
 *     <li>A jboss-web.xml is added, binding to the context path if provided,
 *     otherwise, to /</li>
 *     <li>WEB-INF/lib/* is populated with the application's dependencies.</li>
 * </ul>
 * @author Bob McWhirter
 */
public class WarDeployment implements Deployment {

    private final static String JBOSS_WEB_CONTENTS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<jboss-web>\n" +
                    "    <context-root>${CONTEXT_PATH}</context-root>\n" +
                    "</jboss-web>";

    protected final WebArchive archive;
    protected boolean webInfLibAdded;
    protected String contextPath;

    public WarDeployment(Container container) throws IOException, ModuleLoadException {
        this( container, null );
    }

    public WarDeployment(Container container, String contextPath) throws IOException, ModuleLoadException {
        this.archive = container.getShrinkWrapDomain().getArchiveFactory().create(WebArchive.class);
        this.contextPath = contextPath;
        if ( this.contextPath == null ) {
            this.contextPath = System.getProperty( "wildfly.swarm.context.path" );
        }
        if ( this.contextPath == null ) {
            this.contextPath = "/";
        }
    }

    protected void ensureJBossWebXml() {
        if (this.archive.contains("WEB-INF/jboss-web.xml")) {
            return;
        }

        this.archive.add(new StringAsset(JBOSS_WEB_CONTENTS.replace("${CONTEXT_PATH}", this.contextPath)), "WEB-INF/jboss-web.xml");
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

    protected void ensureWebInfLib() {
        if (this.webInfLibAdded) {
            return;
        }
        this.webInfLibAdded = true;
        InputStream depsTxt = ClassLoader.getSystemClassLoader().getResourceAsStream("META-INF/wildfly-swarm-dependencies.txt");

        if (depsTxt != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(depsTxt))) {

                String line = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        File artifact = MavenArtifactUtil.resolveJarArtifact(line);
                        try (FileInputStream artifactIn = new FileInputStream(artifact)) {
                            this.archive.addAsLibrary(new ByteArrayAsset(artifactIn), artifact.getName());
                        }
                    }
                }
                depsTxt.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public WebArchive getArchive() {
        ensureJBossWebXml();
        ensureWebInfLib();

        /*
        System.err.println( ">>>>>>>>>>>>>" );
        Map<ArchivePath, Node> content = this.archive.getContent();
        for( Map.Entry each : content.entrySet() ) {
            System.err.println( each );
        }
        System.err.println( "<<<<<<<<<<<<<" );
        */
        return this.archive;
    }
}
