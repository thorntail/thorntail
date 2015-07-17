package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.*;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.impl.base.container.WebContainerBase;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;
import org.objectweb.asm.ClassReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class JAXRSArchiveImpl extends WebContainerBase<JAXRSArchive> implements JAXRSArchive {

    // -------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(WebArchiveImpl.class.getName());

    /**
     * Path to the web inside of the Archive.
     */
    private static final ArchivePath PATH_WEB = ArchivePaths.root();

    /**
     * Path to the WEB-INF inside of the Archive.
     */
    private static final ArchivePath PATH_WEB_INF = ArchivePaths.create("WEB-INF");

    /**
     * Path to the resources inside of the Archive.
     */
    private static final ArchivePath PATH_RESOURCE = ArchivePaths.create(PATH_WEB_INF, "classes");

    /**
     * Path to the libraries inside of the Archive.
     */
    private static final ArchivePath PATH_LIBRARY = ArchivePaths.create(PATH_WEB_INF, "lib");

    /**
     * Path to the classes inside of the Archive.
     */
    private static final ArchivePath PATH_CLASSES = ArchivePaths.create(PATH_WEB_INF, "classes");

    /**
     * Path to the manifests inside of the Archive.
     */
    private static final ArchivePath PATH_MANIFEST = ArchivePaths.create("META-INF");

    /**
     * Path to web archive service providers.
     */
    private static final ArchivePath PATH_SERVICE_PROVIDERS = ArchivePaths.create(PATH_CLASSES, "META-INF/services");

    // -------------------------------------------------------------------------------------||
    // Instance Members -------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    // -------------------------------------------------------------------------------------||
    // Constructor ------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Create a new JAXRS Archive with any type storage engine as backing.
     *
     * @param delegate The storage backing.
     */
    public JAXRSArchiveImpl(Archive<?> delegate) {
        super(JAXRSArchive.class, delegate);

        setDefaultContextRoot();
        addGeneratedApplication();
        addExceptionMapperForFavicon();
    }

    protected void addGeneratedApplication() {

        Map<ArchivePath, Node> content = getArchive().getContent();

        boolean applicationFound = false;

        for (Map.Entry<ArchivePath, Node> entry : content.entrySet()) {
            Node node = entry.getValue();
            Asset asset = node.getAsset();
            if (hasApplicationPathAnnotation(node.getPath(), asset)) {
                applicationFound = true;
                break;
            }
        }

        if (!applicationFound) {
            String name = "org.wildfly.swarm.generated.WildFlySwarmDefaultJAXRSApplication";
            String path = "WEB-INF/classes/" + name.replace('.', '/') + ".class";

            byte[] generatedApp = ApplicationFactory.create(name, "/");
            add(new ByteArrayAsset(generatedApp), path);

            addHandlers(new ApplicationHandler(this, path));
        }
    }

    protected void addExceptionMapperForFavicon() {
        try {
            add(new ByteArrayAsset(FaviconExceptionMapperFactory.create()), "WEB-INF/classes/org/wildfly/swarm/generated/FaviconExceptionMapper.class");
            addClass(FaviconHandler.class);
            addModule("org.jboss.modules");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JAXRSArchive addResource(Class<?> resource) {
        addClass(resource);
        return covarientReturn();
    }


    // -------------------------------------------------------------------------------------||
    // Required Implementations -----------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getManifestPath()
     */
    @Override
    protected ArchivePath getManifestPath() {
        return PATH_MANIFEST;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getClassesPath()
     */
    @Override
    protected ArchivePath getClassesPath() {
        return PATH_CLASSES;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getResourcePath()
     */
    @Override
    protected ArchivePath getResourcePath() {
        return PATH_RESOURCE;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getLibraryPath()
     */
    @Override
    protected ArchivePath getLibraryPath() {
        return PATH_LIBRARY;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.WebContainerBase#getWebPath()
     */
    @Override
    protected ArchivePath getWebPath() {
        return PATH_WEB;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.WebContainerBase#getWebInfPath()
     */
    @Override
    protected ArchivePath getWebInfPath() {
        return PATH_WEB_INF;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.WebContainerBase#getWebInfPath()
     */
    @Override
    protected ArchivePath getServiceProvidersPath() {
        return PATH_SERVICE_PROVIDERS;
    }


    private static boolean hasApplicationPathAnnotation(ArchivePath path, Asset asset) {
        if (asset == null) {
            return false;
        }

        if (! path.get().endsWith(".class" ) ) {
            return false;
        }

        try (InputStream in = asset.openStream() ) {
            ClassReader reader = new ClassReader(in);
            AnnotationSeekingClassVisitor visitor = new AnnotationSeekingClassVisitor();
            reader.accept(visitor, 0 );
            return visitor.isFound();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return false;
    }

    public static class ApplicationHandler implements ArchiveEventHandler {

        private final JAXRSArchive archive;
        private final String path;

        public ApplicationHandler(JAXRSArchive archive, String path) {
            this.archive = archive;
            this.path = path;
        }


        @Override
        public void handle(ArchiveEvent event) {
            Asset asset = event.getAsset();
            if (hasApplicationPathAnnotation(event.getPath(), asset)) {
                this.archive.delete(this.path);
            }
        }
    }
}
