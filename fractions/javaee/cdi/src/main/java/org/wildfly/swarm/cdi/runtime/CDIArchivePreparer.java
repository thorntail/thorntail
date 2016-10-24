package org.wildfly.swarm.cdi.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.wildfly.swarm.spi.api.ArchivePreparer;

/** Ensures that a <code>beans.xml</code> is present in the archive.
 *
 * @author Bob McWhirter
 */
public class CDIArchivePreparer implements ArchivePreparer {

    public static final String META_INF_BEANS_XML = "META-INF/beans.xml";

    public static final String WEB_INF_BEANS_XML = "WEB-INF/beans.xml";

    @Override
    public void prepareArchive(Archive<?> archive) {

        if (archive.getName().endsWith(".jar")) {
            Node node = archive.get(META_INF_BEANS_XML);
            if (node != null && node.getAsset() != null) {
                return;
            }

            archive.add(EmptyAsset.INSTANCE, META_INF_BEANS_XML);
        }

        if (archive.getName().endsWith(".war")) {
            Node node = archive.get(WEB_INF_BEANS_XML);
            if (node != null && node.getAsset() != null) {
                return;
            }
            archive.add(EmptyAsset.INSTANCE, WEB_INF_BEANS_XML);
        }
    }
}
