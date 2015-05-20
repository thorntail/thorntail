package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchiveFormat;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.ExtensionLoader;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;

/**
 * @author Bob McWhirter
 */
public class ShrinkWrapExtensionLoader implements ExtensionLoader {

    @Override
    public <T extends Assignable> T load(Class<T> extensionClass, Archive<?> baseArchive) {
        System.err.println( "LOAD: " + extensionClass + ", " + baseArchive );
        if ( extensionClass.equals( WebArchive.class) ) {
            return (T) new WebArchiveImpl(baseArchive);
        }
        return null;
    }

    @Override
    public <T extends Assignable> ExtensionLoader addOverride(Class<T> extensionClass, Class<? extends T> extensionImplClass) {
        System.err.println( "ADD OVERRIDE: " + extensionClass + ", " + extensionImplClass );
        return null;
    }

    @Override
    public <T extends Assignable> String getExtensionFromExtensionMapping(Class<T> extensionClass) {
        System.err.println( "getExtensionFromMapping: " + extensionClass );
        if ( extensionClass.equals(WebArchive.class) ) {
            System.err.println( "return .war" );
            return ".war";
        }
        return null;
    }

    @Override
    public <T extends Archive<T>> ArchiveFormat getArchiveFormatFromExtensionMapping(Class<T> extensionClass) {
        System.err.println( "getArchiveExtensionFromMapping: " + extensionClass );
        return null;
    }
}
