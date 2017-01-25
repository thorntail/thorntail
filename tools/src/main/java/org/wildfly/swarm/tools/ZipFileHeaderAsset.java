package org.wildfly.swarm.tools;

import java.io.InputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ZipFileHeaderAsset implements Asset {

    private final ZipFile zipFile;

    private final FileHeader fileHeader;

    public ZipFileHeaderAsset(ZipFile zipFile, FileHeader fileHeader) {
        this.zipFile = zipFile;
        this.fileHeader = fileHeader;
    }

    @Override
    public InputStream openStream() {
        try {
            return zipFile.getInputStream(fileHeader);
        } catch (ZipException e) {
            throw new RuntimeException("Could not open zip file stream", e);
        }
    }
}
