package org.wildfly.swarm.spi.meta;

import java.nio.file.Path;

/**
 *
 * @author Juan Gonzalez
 *
 */
public class FileSource {

    private Path basePath;
    private Path source;

    public FileSource(Path basePath, Path source) {
        super();
        this.basePath = basePath;
        this.source = source;
    }

    public Path getBasePath() {
       return basePath;
    }

    public void setBasePath(Path path) {
        this.basePath = path;
    }

    public Path getSource() {
       return source;
    }

    public void setSource(Path source) {
       this.source = source;
    }

    /**
     * Gets the relative file path, instead of the absolute.
     * @return Relative path from this file, if basePath was provided.
     */
    public String getRelativePath() {
       if (basePath == null) {
           return source.toString();
       }

       return basePath.relativize(source).toString();
    }

}