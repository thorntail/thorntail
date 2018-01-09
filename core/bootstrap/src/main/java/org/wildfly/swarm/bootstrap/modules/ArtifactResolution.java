package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

public interface ArtifactResolution {

    enum Type {
        FILE,
        STREAM
    }

    String getName();

    Type getType();

    File getFile() throws IOException;

    default boolean isFile() {
        return getType() == Type.FILE;
    }

    InputStream openStream() throws FileNotFoundException;

    class FileArtifactResolution implements ArtifactResolution {


        public FileArtifactResolution(ArtifactCoordinates coords, String packaging, File file) {
            this.coords = coords;
            this.packaging = packaging;
            this.file = file;
            ArtifactResolutionCache.CACHED_FILES.put(coords.toString() + "/" + packaging, file);
        }

        public String getName() {
            return this.file.getName();
        }

        @Override
        public Type getType() {
            return Type.FILE;
        }

        @Override
        public File getFile() {
            return this.file;
        }

        @Override
        public InputStream openStream() throws FileNotFoundException {
            return new FileInputStream(this.file);
        }

        private final ArtifactCoordinates coords;
        private final String packaging;
        private final File file;

    }

    class StreamArtifactResolution implements ArtifactResolution {

        public StreamArtifactResolution(ArtifactCoordinates coords, String packaging, String name, InputStream stream) {
            this.coords = coords;
            this.packaging = packaging;
            this.name = name;
            this.stream = stream;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public Type getType() {
            return Type.STREAM;
        }

        @Override
        public synchronized File getFile() throws IOException {
            File file = ArtifactResolutionCache.CACHED_FILES.get(this.coords.toString() + "/" + this.packaging);
            if (file == null) {
                file = copyTempJar(name, openStream(), "jar");
                ArtifactResolutionCache.CACHED_FILES.put(this.coords.toString() + "/" + this.packaging, file);
            }
            return file;
        }

        public static File copyTempJar(String name, InputStream in, String packaging) throws IOException {
            File tmp = TempFileManager.INSTANCE.newTempFile(name, "." + packaging);
            Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }

        @Override
        public synchronized InputStream openStream() throws FileNotFoundException {
            if (this.stream == null) {
                throw new UnsupportedOperationException("Stream already consumed");
            }
            InputStream s = this.stream;
            this.stream = null;
            return s;
        }

        private final ArtifactCoordinates coords;
        private final String packaging;
        private InputStream stream;
        private final String name;
    }


}
