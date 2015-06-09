package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchiveFormat;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.ExtensionLoader;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.jboss.shrinkwrap.impl.base.spec.JavaArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;

/**
 * @author Bob McWhirter
 */
public class ShrinkWrapExtensionLoader implements ExtensionLoader {

    @Override
    public <T extends Assignable> T load(Class<T> extensionClass, Archive<?> baseArchive) {
        if (extensionClass.equals(WebArchive.class)) {
            return (T) new WebArchiveImpl(baseArchive);
        } else if (extensionClass.equals(JavaArchive.class)) {
            return (T) new JavaArchiveImpl(baseArchive);
        } else if ( extensionClass.equals(ZipExporter.class) ) {
            return (T) new ZipExporterImpl(baseArchive);
        }
        return null;
    }

    @Override
    public <T extends Assignable> ExtensionLoader addOverride(Class<T> extensionClass, Class<? extends T> extensionImplClass) {
        return null;
    }

    @Override
    public <T extends Assignable> String getExtensionFromExtensionMapping(Class<T> extensionClass) {
        if (extensionClass.equals(WebArchive.class)) {
            return ".war";
        } else if (extensionClass.equals(JavaArchive.class)) {
            return ".jar";
        }
        return null;
    }

    @Override
    public <T extends Archive<T>> ArchiveFormat getArchiveFormatFromExtensionMapping(Class<T> extensionClass) {
        return null;
    }
}
