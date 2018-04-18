package io.thorntail.jca;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.metadata.spec.RaParser;

/**
 * Factory capable of creating {@link ResourceAdapterDeployment} instances from {@code ra.xml}-formated files on the classpath.
 *
 * <p>This factory may be {@code @Inject}ed into application components.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
@ApplicationScoped
public class ResourceAdapterDeploymentFactory {

    private static final String RA_XML_SUFFIX = "-ra.xml";

    private static final String RAR_SUFFIX = ".rar";

    /**
     * Create a new deployment descriptor given a classpath resource location.
     *
     * <p>The format of the resource should strictly adhere to the {@code ra.xml} format.</p>
     * <p>The unique identifier for the deployment will be derived from the resource file name.
     * For instance, a resource location of {@code META-INF/my-eis-ra.xml} will result
     * in a unique identifier of {@code my-eis}. For more control over naming, please
     * use {@link #create(String, String)}.</p>
     *
     * @param raXmlPath The classpath resource location.
     * @return The new resource-adapter deployment description.
     * @throws Exception if an error occurs while locating, loading or parsing the file.
     */
    public ResourceAdapterDeployment create(String raXmlPath) throws Exception {
        return create(idOf(raXmlPath), raXmlPath);
    }

    /**
     * Create a new deployment descriptor given a classpath resource location.
     *
     * <p>The format of the resource should strictly adhere to the {@code ra.xml} format.</p>
     *
     * @param id        The unique identifier for the resource adapter.
     * @param raXmlPath The classpath resource location.
     * @return The new resource-adapter deployment description.
     * @throws Exception if an error occurs while locating, loading or parsing the file.
     */
    public ResourceAdapterDeployment create(String id, String raXmlPath) throws Exception {

        URL url = Thread.currentThread().getContextClassLoader().getResource(raXmlPath);
        if (url == null) {
            return null;
        }
        try (InputStream in = url.openStream()) {
            Connector connector = this.parser.parse(in);

            return new ResourceAdapterDeployment(id, new File(id + RAR_SUFFIX), connector);
        }
    }

    String idOf(String path) {
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash < 0) {
            return path;
        }
        String id = path.substring(lastSlash + 1);
        if (id.endsWith(RA_XML_SUFFIX)) {
            id = id.substring(0, id.length() - RA_XML_SUFFIX.length());
        }

        return id;
    }

    @Inject
    RaParser parser;
}
