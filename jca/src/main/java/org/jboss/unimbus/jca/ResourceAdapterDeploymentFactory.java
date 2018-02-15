package org.jboss.unimbus.jca;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.metadata.spec.RaParser;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class ResourceAdapterDeploymentFactory {

    private static final String RA_XML_SUFFIX = "-ra.xml";

    private static final String RAR_SUFFIX = ".rar";

    public ResourceAdapterDeployment create(String raXmlPath) throws Exception {
        return create( idOf(raXmlPath), raXmlPath);
    }

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
