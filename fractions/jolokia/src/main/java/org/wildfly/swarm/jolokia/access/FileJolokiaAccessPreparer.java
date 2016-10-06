package org.wildfly.swarm.jolokia.access;

import java.io.File;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.FileAsset;

/**
 * @author Bob McWhirter
 */
public class FileJolokiaAccessPreparer extends AbstractJolokiaAccessPreparer {

    public FileJolokiaAccessPreparer(File file) {
        this.file = file;
    }

    @Override
    protected Asset getJolokiaAccessXmlAsset() {
        if ( this.file.exists() ) {
            return new FileAsset(this.file);
        }
        return null;
    }

    private final File file;

}
